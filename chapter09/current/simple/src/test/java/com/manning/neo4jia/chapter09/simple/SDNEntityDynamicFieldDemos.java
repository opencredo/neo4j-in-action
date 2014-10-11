package com.manning.neo4jia.chapter09.simple;

import com.manning.neo4jia.chapter09.simple.domain.User;
import com.manning.neo4jia.chapter09.simple.util.SocialNetworkUniverse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
 * (Via JUnit type tests), this class aims to demonstrate how to make use
 * of the various @Query annotated fields defined on SDN Entities.
 *
 * This class makes use of the helper SocialNetworkUniverse class to create a
 * known set of Users, Movies and relationships in the system (graph DB)
 * at the start of each method, against which the queries are then performed.
 */
@ContextConfiguration(locations = {"classpath*:/test-simple-application-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(defaultRollback = false)
public class SDNEntityDynamicFieldDemos {

    private static final Logger logger = LoggerFactory.getLogger(SDNEntityDynamicFieldDemos.class);

    @Autowired
    private Neo4jTemplate template;
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
    public void demoGetFriendsOfFriendsStyle1() {
        Iterable<User> fofs1 = john.getFriendsOfFriends();
        assertExpectedNodeIds(fofs1, Arrays.asList(
                socialNetworkUniverse.tom.getNodeId(),
                socialNetworkUniverse.susan.getNodeId(),
                socialNetworkUniverse.pam.getNodeId()));
    }

    @Test
    @Transactional
    public void demoGetFriendsOfFriendsStyle2() {
        // Same query but uses dynamic params to specify type of relationship
        Iterable<User> fofs2 = john.getFriendsOfFriendsStyle2();
        assertExpectedNodeIds(fofs2, Arrays.asList(
                socialNetworkUniverse.tom.getNodeId(),
                socialNetworkUniverse.susan.getNodeId(),
                socialNetworkUniverse.pam.getNodeId()));
    }

    @Test
    @Transactional
    public void demoCountNumFriendsEachOfMyDirectFriendsHave() {

        Iterable<Map<String, Object>> numFofs = john.getFriendsOfFriendsCount();

        Map<String,Long> flattenedResult = new HashMap<String, Long>();
        for (Map<String, Object> entry: numFofs) {
            flattenedResult.put((String) entry.get("friendName"), (Long)entry.get("numFriends"));
        }

        //logger.info("flattenedResult=" + flattenedResult);
        assertEquals("Total Num friend of friends wrong ", 2, flattenedResult.size());
        Long numJackFOFs = flattenedResult.get("Jack");
        Long numKateFOFs = flattenedResult.get("Kate");
        assertNotNull("Jack should have been included as a friend", numJackFOFs);
        assertNotNull("Kate should have been included as a friend", numKateFOFs);
        assertEquals("incorrect num FOFs for Jack ", (Long) 2L, numJackFOFs); // pam and susan
        assertEquals("incorrect num FOFs for Kate", (Long) 2L, numKateFOFs);  // susan and tom
    }

    @Test
    @Transactional
    public void demoGetTotalNumFriendsOfFriends() {

        Long num = john.getTotalNumFriendsOfFriends();
        assertEquals("Total Num friend of friends wrong ", (Long) 3L, num);

    }

    private void assertExpectedNodeIds(Iterable<User> fofs, List<Long> expectedNodeIds) {
        Set<Long> actualNodeIds =  new HashSet<Long>();
        for (User fof: fofs) {
            actualNodeIds.add(fof.getNodeId());
        }
        assertEquals("Unexpected number of fofs", expectedNodeIds.size(), actualNodeIds.size());
        assertTrue("Invalid users returned", actualNodeIds.containsAll(expectedNodeIds));
    }


}



