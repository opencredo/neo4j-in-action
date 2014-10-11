package com.manning.neo4jia.chapter09.simple.util;

import com.manning.neo4jia.chapter09.simple.domain.Movie;
import com.manning.neo4jia.chapter09.simple.domain.User;
import com.manning.neo4jia.chapter09.simple.domain.Viewing;
import org.neo4j.graphdb.Transaction;
import org.springframework.data.neo4j.support.Neo4jTemplate;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Utility class which sets up the sample / demo social network universe in
 * the neo4j graph database, and provides some utility methods for operating
 * on the base graph entities.
 *
 * Many of the various Demo/Test classes rely on this setup in order to
 * demonstrate various features of SDN.
 */
public class SocialNetworkUniverse {

    private Neo4jTemplate template;
    private GraphUtil graphUtil;
    public User john;
    public User jack;
    public User kate;

    public User susan;
    public User pam;
    public User tom;

    private Movie alienTheMovie;

    public SocialNetworkUniverse(Neo4jTemplate template) {
        this.template = template;
        this.graphUtil = new GraphUtil(template);
    }

    public void init() {
        setupAndSaveAllBaseUsers();
        addFriendsToUsers();
        setupAndSaveMoviesViewingsAndReferals();

        template.save(john);
        template.save(jack);
        template.save(pam);
        template.save(susan);
        template.save(kate);
        template.save(tom);

        tom   = template.findOne(tom.getNodeId(), User.class);
        susan = template.findOne(susan.getNodeId(), User.class);
        pam   = template.findOne(pam.getNodeId(), User.class);
        kate  = template.findOne(kate.getNodeId(), User.class);
        jack  = template.findOne(jack.getNodeId(), User.class);
        john  = template.findOne(john.getNodeId(), User.class);

        alienTheMovie = template.findOne(alienTheMovie.getNodeId(), Movie.class);

        assertUniverseIsValid();

    }

    public void setupAndSaveAllBaseUsers() {

        // We need to save these users in order to get a node id
        // allocated ... before we add them as friends with each other
        // etc
        tom   = template.save(new User("tom001", "Tom"));
        susan = template.save(new User("susan001", "Susan"));
        pam   = template.save(new User("pam001", "Pam"));
        kate  = template.save(new User("kate001", "Kate"));
        jack  = template.save(new User("jack001", "Jack"));
        john  = template.save(new User("john001", "John"));

        john.setReferredBy(susan);
    }

    private void setupAndSaveMoviesViewingsAndReferals() {
        alienTheMovie = template.save(new Movie("Alien"));
        john.addViewing(alienTheMovie, Viewing.FOUR_STARS);
        susan.addViewing(alienTheMovie,Viewing.THREE_STARS);
    }

    /**
     * Configure (by adding friends) the known friend of
     * friends scenario
     */
    private void addFriendsToUsers() {
        john.addFriend(jack);
        john.addFriend(kate);
        template.save(john);
        john   = template.findOne(john.getNodeId(), User.class);
        jack   = template.findOne(jack.getNodeId(), User.class);
        kate   = template.findOne(kate.getNodeId(), User.class);

        jack.addFriend(pam);
        jack.addFriend(susan);
        template.save(jack);
        jack    = template.findOne(jack.getNodeId(), User.class);
        pam     = template.findOne(pam.getNodeId(), User.class);
        susan   = template.findOne(susan.getNodeId(), User.class);

        kate.addFriend(susan);
        template.save(kate);
        kate    = template.findOne(kate.getNodeId(), User.class);
        susan   = template.findOne(susan.getNodeId(), User.class);

        tom.addFriend(kate);
        template.save(tom);
        kate    = template.findOne(kate.getNodeId(), User.class);
        tom     = template.findOne(tom.getNodeId(), User.class);

    }

    public void assertUniverseIsValid() {
        assertExpectedFriendsOfFriends();
        assertExpectedMoviesAndViewings();
    }

    private void assertExpectedMoviesAndViewings() {
        Movie alien = template.findOne(alienTheMovie.getNodeId(), Movie.class);
        Map<User,Integer> expectedRatings = new HashMap<User,Integer>();
        expectedRatings.put(john,Viewing.FOUR_STARS);
        expectedRatings.put(susan,Viewing.THREE_STARS);

        Map<User,Integer> userRatings = new HashMap<User,Integer>();

        for (Viewing view : alien.getViews()) {
            userRatings.put(view.getUser(),view.getStars());
        }

        assertEquals(expectedRatings, userRatings);

    }

    /**
     * Validate that the friends of friends setup has
     * been done correctly
     */
    public void assertExpectedFriendsOfFriends() {
        loadAndValidateFriends(john.getNodeId(),  new HashSet(Arrays.asList(jack.getNodeId(), kate.getNodeId())));
        loadAndValidateFriends(jack.getNodeId(),  new HashSet(Arrays.asList(pam.getNodeId(), susan.getNodeId(),john.getNodeId())));
        loadAndValidateFriends(kate.getNodeId(),  new HashSet(Arrays.asList(tom.getNodeId(), susan.getNodeId(),john.getNodeId())));
        loadAndValidateFriends(pam.getNodeId(),   new HashSet(Arrays.asList(jack.getNodeId())));
        loadAndValidateFriends(susan.getNodeId(), new HashSet(Arrays.asList(jack.getNodeId(),kate.getNodeId())));
        loadAndValidateFriends(tom.getNodeId(),   new HashSet(Arrays.asList(kate.getNodeId())));
    }

    /**
     * Load a particular user and check whether their friends match those
     * which are expected.
     */
    protected void loadAndValidateFriends(Long nodeId, Set<Long> expectedFriendIds) {
        User loadedUser = template.findOne(nodeId, User.class);
        Set<User> friends = loadedUser.getFriends();
        validateFriends(friends, expectedFriendIds);
    }

    /**
     * Make sure that the actual friends and expected friends match
     */
    public void validateFriends(Iterable<User> friends, Long... expectedFriendId) {
        Set<Long> expectedIds =  new HashSet<Long>();
        for (Long expectedId: expectedFriendId) {
            expectedIds.add(expectedId);
        }
        validateFriends(friends,expectedIds);
    }

    /**
     * Make sure that the actual friends and expected friends match
     */
    protected void validateFriends(Iterable<User> friends, Set<Long> expectedFriendIds) {
        Set<Long> actualFriends = new HashSet<Long>();
        for (User friend: friends) {
            actualFriends.add(friend.getNodeId());
        }

        assertEquals("incorrect num friends" , expectedFriendIds.size() , actualFriends.size());
        assertEquals("incorrect friends" , expectedFriendIds , actualFriends);
    }



    public void bypassSDNAndCreateANewReferralRel(long userNodeId, long referredByUserNodeId) {
        template.createRelationshipBetween(template.getNode(userNodeId),
                template.getNode(referredByUserNodeId),
                SocialNetworkRelationshipType.referredBy.name(),
                new HashMap());
    }

    public void createAndSaveUserToDBInSeparateTX(User user) {
        Transaction tx = template.getGraphDatabaseService().beginTx();
        try {
            template.save(user);
            tx.success();
        } finally {
            tx.finish();
        }
    }
}
