package com.manning.neo4jia.chapter09.simple.indexbasedtrs;

import com.manning.neo4jia.chapter09.simple.indexbasedtrs.domain.User;
import com.manning.neo4jia.chapter09.simple.indexbasedtrs.util.SocialNetworkUniverse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.conversion.Result;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.data.neo4j.support.node.Neo4jHelper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * (Via JUnit type tests), this class aims to demonstrate how to use
 * the Neo4JTemplate class to perform basic queries where SDN
 * entities are involved.
 * <p/>
 * This class makes use of the helper SocialNetworkUniverse class to create a
 * known set of Users, Movies and relationships in the system (graph DB)
 * at the start of each method, against which the queries are then performed.
 */
@ContextConfiguration(locations = {"classpath*:/indexbasedtrs/test-simple-application-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(defaultRollback = false)
public class Neo4JTemplateQueryingDemos {

    @Autowired
    private Neo4jTemplate template;
    private SocialNetworkUniverse socialNetworkUniverse;

    @BeforeTransaction
    public void cleanDb() {
        Neo4jHelper.cleanDb(template.getGraphDatabaseService());
    }

    @Before
    public void setUp() {
        socialNetworkUniverse = new SocialNetworkUniverse(template);
        socialNetworkUniverse.init();
    }

    @Transactional
    @Test
    public void demoFindAUsersFriendsOfFriendsUsingTheirUserId() {

        String query =
                "start n=node:User(userId = {userId}) " +
                        "match (n)-[r:IS_FRIEND_OF]-(friend)-[r2:IS_FRIEND_OF]-(fof) " +
                        "return distinct fof";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", "john001");
        Result<Map<String, Object>> result = template.query(query, params);
        Result<User> userResults = result.to(User.class);
        assertExpectedFriendsOfFriends(userResults);
    }

    @Transactional
    @Test
    public void demoFindAUsersFriendsOfFriendsUsingTheirNodeId() {

        // Note: Generally you would not use nodeId's directly but rather
        //       lookup entities based on indexed properties as above,
        //       however this method provides a way to see the difference
        //       in the two mechanisms.

        String query =
                "start n=node({nodeId}) " +
                        "match (n)-[r:IS_FRIEND_OF]-(friend)-[r2:IS_FRIEND_OF]-(fof) " +
                        "return distinct fof";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("nodeId", socialNetworkUniverse.john.getNodeId());
        Result<Map<String, Object>> result = template.query(query, params);
        Result<User> userResults = result.to(User.class);
        assertExpectedFriendsOfFriends(userResults);

    }

    private void assertExpectedFriendsOfFriends(Result<User> userResults) {
        List<String> extractedUserIds = new ArrayList<String>();
        Iterator<User> it = userResults.iterator();
        while (it.hasNext()) {
            User val = it.next();
            extractedUserIds.add(val.getUserId());
        }

        assertEquals("Expecting 3 fofs", 3, extractedUserIds.size());
        assertTrue("Invalid users returned", extractedUserIds.containsAll(
                Arrays.asList("tom001", "susan001", "pam001")));
    }
}



