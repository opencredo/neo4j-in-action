package com.manning.neo4jia.chapter09.simple;

import com.manning.neo4jia.chapter09.simple.domain.Movie;
import com.manning.neo4jia.chapter09.simple.domain.PhoneNumber;
import com.manning.neo4jia.chapter09.simple.domain.User;
import com.manning.neo4jia.chapter09.simple.domain.Viewing;
import com.manning.neo4jia.chapter09.simple.repository.MovieRepository;
import com.manning.neo4jia.chapter09.simple.repository.UserRepository;
import com.manning.neo4jia.chapter09.simple.util.GraphUtil;
import com.manning.neo4jia.chapter09.simple.util.SimulatedIssueException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.data.neo4j.support.node.Neo4jHelper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * (Via JUnit type tests), this class aims to demonstrate how to use
 * SDN Repositories to perform core entity persistence type
 * operations (i.e. saving and loading SDN entities).
 */
@ContextConfiguration(locations = {"classpath*:/test-simple-application-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(defaultRollback = false)
public class RepositoryBasedCoreEntityPersistenceDemos {


    @Autowired
    private Neo4jTemplate template;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    PlatformTransactionManager neo4jTransactionManager;

    private GraphUtil graphUtil;

    @BeforeTransaction
    public void cleanDb() {
        Neo4jHelper.cleanDb(template.getGraphDatabaseService());
    }

    @Before
    public void setUp() throws Exception {
        this.graphUtil = new GraphUtil(template);
    }

    // Demostrates find by node id of listing 9.9
    @Test
    @Transactional
    public void demoSaveUserAndThenFindByNodeId() {
        User user      = new User("john001","John");
        userRepository.save(user);

        User loadedUser = userRepository.findOne(user.getNodeId());
        assertEquals("retrieved user does not match saved user", user, loadedUser);
    }

    // Demostrates find by indexed user id of listing 9.9
    @Test
    @Transactional
    public void demoSaveUserAndThenLookupByIndexedUserId() {
        User user      = new User("john001","John");
        userRepository.save(user);

        User loadedUserViaIndex = userRepository.findBySchemaPropertyValue("userId", "john001");
        assertEquals("retrieved user does not match saved user", user, loadedUserViaIndex);
    }

    @Test
    @Transactional
    public void demoSaveUserAndThenFindNativelyUsingCypher() {
        User user      = new User("john001","John");
        userRepository.save(user);

        Map<String,Object> params = new HashMap<String,Object>();
        params.put("userId","john001");
        Node johnNode = template.query("MATCH (n:User) WHERE n.userId = {userId} return n ", params).to(Node.class).singleOrNull();

        assertNotNull("Should have been able to lookup john using indexed userId",johnNode);
        assertEquals("node retrieved does not match expected user", (Long) user.getNodeId(), (Long) johnNode.getId());

    }

    @Test
    @Transactional
    public void demoSavingAndLoadingMultipleSDNEntities() {
        Movie alien      = new Movie("Alien");
        User john        = new User("john001" , "John");
        User sally       = new User("sally001", "Sally");
        User other       = new User("user003","Other Scott");
        User zane        = new User("user004","Zane Scott");

        movieRepository.save(alien);
        userRepository.save(john);
        userRepository.save(sally);
        userRepository.save(other);
        userRepository.save(zane);

        john.setReferredBy(sally);
        john.addFriend(sally);

        userRepository.save(john);
        sally   = userRepository.findOne(sally.getNodeId());
        // if we don't do the above (or alternatively the line commented  below) we will
        // lose sally as johns friend when we save Sally.
        // This is because of the way in which the Direction.BOTH is handled for the friends
        // relationship.
        //sally.addFriend(john);
        sally.addViewing(alien,3);

        movieRepository.save(alien);
        userRepository.save(john);
        userRepository.save(sally);


        Movie retrievedMovie = movieRepository.findOne(alien.getNodeId());
        assertEquals("retrieved movie matches persisted one", alien, retrievedMovie);
        assertEquals("retrieved movie title matches", "Alien", retrievedMovie.getTitle());

        User retrievedSallyUser = userRepository.findOne(sally.getNodeId());
        assertEquals("retrieved user sally matches persisted one", sally, retrievedSallyUser);
        assertEquals("retrieved user sally name matches", "Sally", retrievedSallyUser.getName());

        User retrievedJohnUser = userRepository.findOne(john.getNodeId());
        assertEquals("retrieved user john matches persisted one", john, retrievedJohnUser);
        assertEquals("retrieved user john name matches", "John", retrievedJohnUser.getName());

        // Assert relationships
        assertTrue("John should be friends with Sally", retrievedJohnUser.getFriends().contains(retrievedSallyUser));
        assertTrue("Sally should be friends with John", retrievedSallyUser.getFriends().contains(retrievedJohnUser));
        assertEquals("Sally should have one review", retrievedSallyUser.getViews().size(), 1);
        Viewing viewing = (Viewing)retrievedSallyUser.getViews().toArray()[0];
        assertEquals("Should have 3 star rating",viewing.getStars(),(Integer)3);
        assertEquals("Should be for forest",viewing.getMovie(),retrievedMovie);
    }


    @Test
    @Transactional
    public void demoSavingOneSDNEntityCanTransitivelyResultInOtherAnotherEntityBeingSaved() {
        User john      = new User("john001","John");
        User susan     = new User("susan001","Susan");

        john.addFriend(susan);

        // We are only saving john , but susan gets saved too
        int beforeSaveNumUsers = graphUtil.count(userRepository.findAll().iterator());
        assertEquals("num users before save invoked is incorrect", 0, beforeSaveNumUsers);

        userRepository.save(john);

        int afterSaveNumUsers = graphUtil.count(userRepository.findAll().iterator());
        assertEquals("num users after save invoked is incorrect",  2, afterSaveNumUsers);

    }


    @Test
    @Transactional
    public void demoDefaultLazyLoading() {

        // 1. Create John and Sally and make them friends
        User john        = new User("john001" , "John");
        User sally       = new User("sally001", "Sally");
        john.addFriend(sally);
        userRepository.save(john);

        // 2. Load Sally, as well as her first friend (which should be John)
        User loadedSally = userRepository.findOne(sally.getNodeId());
        assertEquals("Expected Susan to only have one friend at this stage ", 1, loadedSally.getFriends().size());

        // 3. As the "friends" field has not explicitly been annotated to
        //    fetch relationships eagerly, it will default to lazy loading,
        //    meaning the only attribute which will be set on the returned
        //    friend objects will be the node id, the name for instance, will
        //    not have been set
        User firstFriendOfSusan = loadedSally.getFriends().iterator().next();
        assertEquals("Expected to find john's node Id as the id of Sally's friend ", john.getNodeId(), firstFriendOfSusan.getNodeId());
        assertEquals(null, firstFriendOfSusan.getName());

        // 4. We need to explicitly tell SDN to "fetch" all the data (Note this requires the use
        //    of the Neo4jTemplate)
        template.fetch(loadedSally.getFriends());
        assertEquals("John", firstFriendOfSusan.getName());

    }

    @Test
    @Transactional
    public void demoCustomSpringConverterForPhoneNumber() {

        // This code illustrates the point detailed in the book under
        // section 9.2.3	Modeling Node Entities - in the box:
        // "What about custom property types?"

        // 1. Create and save a user with some phone numbers
        User user = new User("user001", "User 1");
        PhoneNumber phoneNumber = new PhoneNumber("123-4567890");
        user.setPhoneNumber(phoneNumber);
        userRepository.save(user);

        // 2. Load the previously saved user
        User loadedUser = userRepository.findOne(user.getNodeId());

        // 3. Verify expectations around PhoneNumber
        assertNotNull("PhoneNumber should not be null", loadedUser.getPhoneNumber());
        assertEquals("PhoneNumber should be converted correctly", "123-4567890", loadedUser.getPhoneNumber().getNumber());

    }

    // Listing 9.10 (Unfortunately the listing is geared towards being
    // book friendly, and cannot be tested in this basic form. Please
    // see tests demoTestableSaveAndLoadScenario1 and demoTestableSaveAndLoadScenario2)
    @Test
    @Transactional
    public void testSaveAndLoad() {

        // 1. Create John and Sally and make them friends
        User john        = new User("john001" , "John");
        userRepository.save(john);
        Long johnNodeId = john.getNodeId();
        User loadedUser = userRepository.findOne(johnNodeId);

        // Start: No changes between these Start/End
        //        markers saved into graph yet
        loadedUser.setName("New Name");

        User susan = new User("susan001","Susan");
        loadedUser.addFriend(susan);
        // End

        User savedUser  = template.save(loadedUser);
    }

    // Listing 9.10 (testable form)
    @Test
    public void demoTestableSaveAndLoadScenario1() {
        Long johnNodeId = createJohnAndPersistIntoDb();
        demoSavingAndLoadingSemantics(true,johnNodeId,"John",0);
    }

    @Test
    public void demoTestableSaveAndLoadScenario2() {
        Long johnNodeId = createJohnAndPersistIntoDb();
        demoSavingAndLoadingSemantics(false,johnNodeId,"New Name",1);
    }

    private void demoSavingAndLoadingSemantics(boolean simulateException,
                                               long johnNodeId,
                                               String expectedName,
                                               int expectedFriendCount) {

        // 1. Verify Johns initial details
        final User loadedUser = userRepository.findOne(johnNodeId);
        assertEquals("name not as expected" , "John", loadedUser.getName());

        try {
            // 2. Modify in memory details
            changeJohnsNameAndAddFriend(loadedUser,simulateException);
        } catch (SimulatedIssueException e) {
            // Ignore for now ...
        }

        User loadedUser2 = userRepository.findOne(johnNodeId);
        assertEquals("name not as expected" , expectedName, loadedUser2.getName());
        assertEquals("num friends not as expected" , expectedFriendCount , loadedUser2.getFriends().size());
    }

    private void changeJohnsNameAndAddFriend(final User loadedUser, final boolean simulateException) {
        new TransactionTemplate(neo4jTransactionManager).execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                // Start: All changes between these Start/End
                //        markers are only performed in memory
                loadedUser.setName("New Name");
                User susan = new User("susan001","Susan");
                loadedUser.addFriend(susan);
                // End
                if (simulateException) {
                    throw new SimulatedIssueException("Simulating something going wrong to exit at this point");
                }
                userRepository.save(loadedUser);
            }
        });
    }

    private Long createJohnAndPersistIntoDb() {
        try (Transaction tx = template.getGraphDatabaseService().beginTx()) {
            User john        = new User("john001" , "John");
            userRepository.save(john);
            tx.success();
            return john.getNodeId();
        }
    }


}



