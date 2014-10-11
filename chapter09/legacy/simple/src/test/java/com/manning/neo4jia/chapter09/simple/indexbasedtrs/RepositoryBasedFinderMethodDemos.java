package com.manning.neo4jia.chapter09.simple.indexbasedtrs;

import com.manning.neo4jia.chapter09.simple.indexbasedtrs.domain.User;
import com.manning.neo4jia.chapter09.simple.indexbasedtrs.repository.UserRepository;
import com.manning.neo4jia.chapter09.simple.indexbasedtrs.util.SocialNetworkUniverse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.data.neo4j.support.node.Neo4jHelper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.Assert.*;

/**
 * (Via JUnit type tests), this class aims to demonstrate how to use
 * the various dynamic finder type methods which can be defined on
 * SDN Repositories.
 *
 * This class makes use of the helper SocialNetworkUniverse class to create a
 * known set of Users, Movies and relationships in the system (graph DB)
 * at the start of each method, against which the queries are then performed.
 */
@ContextConfiguration(locations = {"classpath*:/indexbasedtrs/test-simple-application-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(defaultRollback = false)
public class RepositoryBasedFinderMethodDemos {

    @Autowired
    private Neo4jTemplate template;
    @Autowired
    private UserRepository userRepository;
    private SocialNetworkUniverse socialNetworkUniverse;

    User john;
    User jack;
    User kate;

    User susan;
    User pam;
    User tom;

    @BeforeTransaction
    public void cleanDb() {
        Neo4jHelper.cleanDb(template.getGraphDatabaseService());
    }

    @Before
    public void setUp() {
        socialNetworkUniverse = new SocialNetworkUniverse(template);
        socialNetworkUniverse.init();

        john = socialNetworkUniverse.john;
        jack = socialNetworkUniverse.jack;
        kate = socialNetworkUniverse.kate;
        susan = socialNetworkUniverse.susan;
        pam = socialNetworkUniverse.pam;
        tom = socialNetworkUniverse.tom;

    }

    @Test
    @Transactional
    public void demoDynamicFindByNameWithSortQueryMethodOnRepository() {

        User bobby200       = new User("bobby200","Bobby");
        User bobby150       = new User("bobby150","Bobby");
        User bobby300       = new User("bobby300","Bobby");
        userRepository.save(bobby200);
        userRepository.save(bobby150);
        userRepository.save(bobby300);

        Sort sortByUserId = new Sort(new Sort.Order(Sort.Direction.ASC,"user.userId"));

        Iterator<User> it = userRepository.findByName("Bobby", sortByUserId).iterator();
        assertTrue("There should be a user in position 1", it.hasNext());
        assertEquals("bobby150 should be first", bobby150.getNodeId(), it.next().getNodeId());

        assertTrue("There should be a user in position 2", it.hasNext());
        assertEquals("bobby200 should be first", bobby200.getNodeId(), it.next().getNodeId());

        assertTrue("There should be a user in position 3", it.hasNext());
        assertEquals("bobby300 should be first", bobby300.getNodeId(), it.next().getNodeId());

        assertFalse("There should be no more users returned in query", it.hasNext());
    }


    @Test
    @Transactional
    public void demoDynamicNameRelatedQueryMethodsOnRepository() {

        User user1 = userRepository.name("Susan").iterator().next();
        assertEquals(user1.getNodeId(), susan.getNodeId());

        User user2 = userRepository.findByName("Susan").iterator().next();
        assertEquals(user2.getNodeId(), susan.getNodeId());

        User user25 = userRepository.findUserByName("Susan").iterator().next();
        assertEquals(user25.getNodeId(), susan.getNodeId());

        User user3 = userRepository.readByName("Susan").iterator().next();
        assertEquals(user3.getNodeId(), susan.getNodeId());

        User user35 = userRepository.readUserByName("Susan").iterator().next();
        assertEquals(user35.getNodeId(), susan.getNodeId());

        User simUser = userRepository.simulateFindByNameWhenUsingIndexedBasedStrategy("Susan").iterator().next();
        assertEquals(simUser.getNodeId(), susan.getNodeId());

        User user4 = userRepository.getByName("Susan").iterator().next();
        assertEquals(user4.getNodeId(), susan.getNodeId());

        User user45 = userRepository.getUserByName("Susan").iterator().next();
        assertEquals(user45.getNodeId(), susan.getNodeId());

        // ----------------------------- \\

        User user6 = userRepository.findDistinctByName("Susan");
        assertEquals(user6.getNodeId(), susan.getNodeId());

        User user5 = userRepository.findDistinctUserByName("Susan");
        assertEquals(user5.getNodeId(), susan.getNodeId());

    }

    @Test
    @Transactional
    public void demoDynamicFindByNameAndFriendsNameMethodOnRepository() {

        User john2       = new User("john002","John 2nd");
        User john3       = new User("john003","John 3rd");
        john2.addFriend(susan);
        john3.addFriend(susan);
        userRepository.save(john2);
        userRepository.save(john3);

        // Find users whose name starts with John AND who has a friend whose name
        // is Susan
        Iterable<User> users = userRepository.findByNameLikeAndFriendsName("John.*","Susan");

        List<String> userNames = new ArrayList<String>();
        for (User user: users) {
            userNames.add(user.getName());
        }
        assertEquals("wrong num users found through repository", 2, userNames.size());
        assertTrue("wrong users returned through repository method", userNames.containsAll(Arrays.asList("John 2nd", "John 3rd")));

        // Compare this to a specific method which simulates doing exactly the
        // same thing but explicitly
        Iterable<User> explicitUserNamesResults = userRepository.simulateFindByNameLikeAndFriendsNameWhenUsingIndexedBasedStrategy("John.*", "Susan");

        List<String> explicitUserNames = new ArrayList<String>();
        for (User user: explicitUserNamesResults) {
            explicitUserNames.add(user.getName());
        }
        assertEquals("wrong num users found through simulated query", 2, explicitUserNames.size());
        assertTrue("wrong users returned through simulated query", explicitUserNames.containsAll(Arrays.asList("John 2nd", "John 3rd")));


    }

    @Test
    @Transactional
    public void demoDynamicGetByNameMethodOnRepository() {

        User user = userRepository.getByName("Susan").iterator().next();
        assertEquals(user.getNodeId(), susan.getNodeId());

    }

    @Test
    @Transactional
    public void demoDynamicFindByReferredByName() {

        john.setReferredBy(susan);
        jack.setReferredBy(susan);
        susan.setReferredBy(tom);
        kate.setReferredBy(susan);
        pam.setReferredBy(john);

        userRepository.save(john);
        userRepository.save(jack);
        userRepository.save(susan);
        userRepository.save(tom);
        userRepository.save(kate);
        userRepository.save(pam);

        Iterable<User> usersReferredBySusan = userRepository.findByReferredByName("Susan");
        socialNetworkUniverse.validateFriends(usersReferredBySusan, john.getNodeId(), jack.getNodeId(), kate.getNodeId());

        Iterable<User> simUsersReferredBySusan = userRepository.simulateFindByReferredByNameWhenUsingIndexedBasedStrategy("Susan");
        socialNetworkUniverse.validateFriends(simUsersReferredBySusan, john.getNodeId(), jack.getNodeId(), kate.getNodeId());



    }

    @Test
    @Transactional
    public void demoDynamicFindByReferredByNameLikeMethod() {

        john.setReferredBy(susan);
        jack.setReferredBy(susan);
        susan.setReferredBy(tom);
        kate.setReferredBy(susan);
        pam.setReferredBy(john);

        userRepository.save(john);
        userRepository.save(jack);
        userRepository.save(susan);
        userRepository.save(tom);
        userRepository.save(kate);
        userRepository.save(pam);

        Iterable<User> usersReferredBySomeoneWithNameStartingWithS = userRepository.findByReferredByNameLike("S.*");
        socialNetworkUniverse.validateFriends(usersReferredBySomeoneWithNameStartingWithS, john.getNodeId(), jack.getNodeId(), kate.getNodeId());

        Iterable<User> simUsersReferredBySomeoneWithNameStartingWithS = userRepository.simulateFindByReferredByNameLikeWhenUsingIndexedBasedStrategy("S.*");
        socialNetworkUniverse.validateFriends(simUsersReferredBySomeoneWithNameStartingWithS, john.getNodeId(), jack.getNodeId(), kate.getNodeId());

    }
}