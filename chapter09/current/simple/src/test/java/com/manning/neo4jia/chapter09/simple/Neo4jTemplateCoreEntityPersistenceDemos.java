package com.manning.neo4jia.chapter09.simple;

import com.manning.neo4jia.chapter09.simple.domain.Movie;
import com.manning.neo4jia.chapter09.simple.domain.User;
import com.manning.neo4jia.chapter09.simple.domain.Viewing;
import com.manning.neo4jia.chapter09.simple.domain.dodgy.UnannotatedUser;
import com.manning.neo4jia.chapter09.simple.util.GraphUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mapping.model.MappingException;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.data.neo4j.support.node.Neo4jHelper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * (Via JUnit type tests), this class aims to demonstrate how to use
 * the Neo4JTemplate class to perform core entity persistence type
 * operations (i.e. saving and loading SDN entities).
 */
@ContextConfiguration(locations = {"classpath*:/test-simple-application-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(defaultRollback = false)
public class Neo4jTemplateCoreEntityPersistenceDemos {

    @Autowired
    private Neo4jTemplate template;
    private GraphUtil graphUtil;

    @Before
    public void setUp() throws Exception {
        this.graphUtil = new GraphUtil(template);
    }

    @BeforeTransaction
    public void cleanDb() {
        Neo4jHelper.cleanDb(template.getGraphDatabaseService());
    }

    @Test(expected = MappingException.class)
    @Transactional
    public void demoExceptionThrownWhenTryingToSaveAnUnannotatedPOJO() {

        UnannotatedUser unannotatedUser = new UnannotatedUser();
        template.save(unannotatedUser);
    }

    @Test
    @Transactional
    public void demoSaveUserAndThenFindByNodeId() {
        // This corresponds closely to the code detailed in
        // Listing 9.8 Neo4jTemplate (shows the case where the lookup
        // is done via the findOne method )
        User user      = new User("john001","John");
        template.save(user);

        User loadedUser = template.findOne(user.getNodeId(), User.class);
        assertEquals("retrieved user does not match saved user", user, loadedUser);
    }

    @Test
    @Transactional
    public void demoSaveUserAndThenLookupByIndexedUserId() {
        // This corresponds closely to the code detailed in
        // Listing 9.8 Neo4jTemplate (shows the case where the lookup
        // is done via the indexed userId field)
        User user      = new User("john001","John");
        template.save(user);

        User loadedUserViaIndex = template
                .findByIndexedValue(User.class,
                        "userId", "john001").singleOrNull();
        assertEquals("User retrieved via index lookup does not match saved user",user, loadedUserViaIndex);
    }

    @Test
    @Transactional
    public void demoSaveUserAndThenFindNativelyUsingCypher() {

        // This corresponds closely to the code detailed in the sidebar box titled-
        // "Under what labels and names are these annotated properties indexed?"
        User user      = new User("john001","John");
        template.save(user);

        Map<String,Object> params = new HashMap<String,Object>();
        params.put("userId","john001");
        Node johnNode = template.query("MATCH (n:User) WHERE n.userId = {userId} return n ", params).to(Node.class).singleOrNull();

        assertNotNull("Should have been able to lookup john using indexed userId",johnNode);
        assertEquals("node retrieved does not match expected user", (Long)user.getNodeId(), (Long)johnNode.getId());

    }

    @Test
    @Transactional
    public void demoSavingAndLoadingMultipleSDNEntities() {
        Movie alien      = new Movie("Alien");
        User john        = new User("john001" , "John");
        User sally       = new User("sally001", "Sally");
        User other       = new User("user003","Other Scott");
        User zane        = new User("user004","Zane Scott");

        template.save(alien);
        template.save(john);
        template.save(sally);
        template.save(other);
        template.save(zane);

        john.setReferredBy(sally);
        john.addFriend(sally);

        template.save(john);
        sally   = template.findOne(sally.getNodeId(), User.class);
        // if we don't do the above (or alternatively the line commented  below) we will
        // lose sally as johns friend when we save Sally.
        // This is because of the way in which the Direction.BOTH is handled for the friends
        // relationship.
        //sally.addFriend(john);
        sally.addViewing(alien,3);

        template.save(alien);
        template.save(john);
        template.save(sally);

        Movie retrievedMovie = template.findOne(alien.getNodeId(), Movie.class);
        assertEquals("retrieved movie matches persisted one", alien, retrievedMovie);
        assertEquals("retrieved movie title matches", "Alien", retrievedMovie.getTitle());

        User retrievedSallyUser = template.findOne(sally.getNodeId(), User.class);
        assertEquals("retrieved user sally matches persisted one", sally, retrievedSallyUser);
        assertEquals("retrieved user sally name matches", "Sally", retrievedSallyUser.getName());

        User retrievedJohnUser = template.findOne(john.getNodeId(), User.class);
        assertEquals("retrieved user john matches persisted one", john, retrievedJohnUser);
        assertEquals("retrieved user john name matches", "John", retrievedJohnUser.getName());

        // Assert relationships
        assertTrue("John should be friends with Sally", retrievedJohnUser.getFriends().contains(retrievedSallyUser));
        assertTrue("Sally should be friends with John", retrievedSallyUser.getFriends().contains(retrievedJohnUser));
        assertEquals("Unexpected num reviews for Sally ", retrievedSallyUser.getViews().size(), 1);
        Viewing viewing = (Viewing)retrievedSallyUser.getViews().toArray()[0];
        assertEquals("Unexpected star rating",viewing.getStars(),Viewing.THREE_STARS);
        assertEquals("Unexpected movie associated",viewing.getMovie(),retrievedMovie);
    }

    @Test
    @Transactional
    public void demoSavingOneSDNEntityCanTransitivelyResultInOtherAnotherEntityBeingSaved() {

        int beforeSaveNumUsers = graphUtil.count(template.findAll(User.class).iterator());
        assertEquals("num users before save invoked is incorrect", 0, beforeSaveNumUsers);

        User john      = new User("john001","John");
        User susan     = new User("susan001","Susan");

        john.addFriend(susan);

        // We are only saving John , but Susan gets saved too
        template.save(john);

        int afterSaveNumUsers = graphUtil.count(template.findAll(User.class).iterator());
        assertEquals("num users after save invoked is incorrect",  2, afterSaveNumUsers);

        User loadedJohn = template.findByIndexedValue(User.class,"userId","john001").singleOrNull();
        User loadedSusan = template.findByIndexedValue(User.class,"userId","susan001").singleOrNull();
        assertNotNull("Should have been able to lookup john after saving", loadedJohn);
        assertNotNull("Should have been able to lookup susan after saving john",loadedSusan);

    }

    // listing 9.8
    @Test
    public void demoSaveUserAndThenFindByNodeIdWithManualTXHandling() {

        GraphDatabaseService graphDB = getGraphDatabase();
        Neo4jTemplate template = new Neo4jTemplate(graphDB);
        try (Transaction tx = template.getGraphDatabaseService().beginTx()) {
            User user      = new User("john001","John");
            User savedUser = template.save(user);

            User loadedUser = template.findOne(savedUser.getNodeId(), User.class);
            assertFalse("Trying to verify that newly loaded object is same as saved but without doing == check ",  savedUser == loadedUser);            assertEquals("retrieved movie matches saved movie",  savedUser, loadedUser);
            assertEquals("retrieved user matches saved user",  savedUser, loadedUser);
            tx.success();
        }
    }

    // Listing 9.11 Extended (with Fetch)
    @Test
    @Transactional
    public void demoDefaultLazyLoading() {

        // 1. Create John and Sally and make them friends
        User john        = new User("john001" , "John");
        User sally       = new User("sally001", "Sally");
        john.addFriend(sally);
        template.save(john);

        // 2. Load Sally, as well as her first friend (which should be John)
        User loadedSally = template.findOne(sally.getNodeId(),User.class);
        assertEquals("Expected Susan to only have one friend at this stage ", 1, loadedSally.getFriends().size());

        // 3. As the "friends" field has not explicitly been annotated to
        //    fetch relationships eagerly, it will default to lazy loading,
        //    meaning the only attribute which will be set on the returned
        //    friend objects will be the node id, the name for instance, will
        //    not have been set
        User firstFriendOfSusan = loadedSally.getFriends().iterator().next();
        assertEquals("Expected to find john's node Id as the id of Sally's friend ", john.getNodeId(), firstFriendOfSusan.getNodeId());
        assertEquals(null, firstFriendOfSusan.getName());

        // 4. Without explicitly telling SDN to "fetch" all the data
        //    the only details populated will be the nodeId and nothing else
        assertEquals(john.getNodeId(), firstFriendOfSusan.getNodeId());
        assertEquals(null, firstFriendOfSusan.getName());

    }

    // Listing 9.11 Extended (with Fetch)
    @Test
    @Transactional
    public void demoDefaultLazyLoadingWithFetch() {

        // 1. Create John and Sally and make them friends
        User john        = new User("john001" , "John");
        User sally       = new User("sally001", "Sally");
        john.addFriend(sally);
        template.save(john);

        // 2. Load Sally, as well as her first friend (which should be John)
        User loadedSally = template.findOne(sally.getNodeId(),User.class);
        assertEquals("Expected Susan to only have one friend at this stage ", 1, loadedSally.getFriends().size());

        // 3. As the "friends" field has not explicitly been annotated to
        //    fetch relationships eagerly, it will default to lazy loading,
        //    meaning the only attribute which will be set on the returned
        //    friend objects will be the node id, the name for instance, will
        //    not have been set
        User firstFriendOfSusan = loadedSally.getFriends().iterator().next();
        assertEquals("Expected to find john's node Id as the id of Sally's friend ", john.getNodeId(), firstFriendOfSusan.getNodeId());
        assertEquals(null, firstFriendOfSusan.getName());

        // 4. We need to explicitly tell SDN to "fetch" all the data
        //    (Note this requires the use of the Neo4jTemplate if the
        //     @Fetch annotation is not used)
        template.fetch(loadedSally.getFriends());
        assertEquals("John", firstFriendOfSusan.getName());

    }


    private GraphDatabaseService getGraphDatabase() {
        return template.getGraphDatabaseService();
    }







}



