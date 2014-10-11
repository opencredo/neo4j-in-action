package com.manning.neo4jia.chapter09.simple.indexbasedtrs;

import com.manning.neo4jia.chapter09.simple.indexbasedtrs.domain.User;
import com.manning.neo4jia.chapter09.simple.indexbasedtrs.repository.UserRepository;
import com.manning.neo4jia.chapter09.simple.indexbasedtrs.util.SocialNetworkUniverse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.graphdb.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.data.neo4j.support.node.Neo4jHelper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * (Via JUnit type tests), this class aims to demonstrate how to use
 * the @Query annotated methods defined on SDN Repositories.
 *
 * This class makes use of the helper SocialNetworkUniverse class to create a
 * known set of Users, Movies and relationships in the system (graph DB)
 * at the start of each method, against which the queries are then performed.
 */
@ContextConfiguration(locations = {"classpath*:/indexbasedtrs/test-simple-application-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(defaultRollback = false)
public class RepositoryBasedQueryMethodDemos {

    @Autowired
    private Neo4jTemplate template;
    @Autowired
    private UserRepository userRepository;
    private SocialNetworkUniverse socialNetworkUniverse;

    @BeforeTransaction
    public void cleanDb() {
        Neo4jHelper.cleanDb(template.getGraphDatabaseService());
    }

    @Before
    public void setUp() {
        socialNetworkUniverse = new SocialNetworkUniverse(template);
        Transaction tx = template.getGraphDatabaseService().beginTx();
        try {
            socialNetworkUniverse.init();
            tx.success();
        } catch (Exception e) {
            tx.failure();
            e.printStackTrace();
            throw new IllegalStateException("Unable to clear data at start of test",e);
        } finally {
            tx.finish();
        }

    }

    @Test
    @Transactional
    public void demoQueryAUsersFriendsOfFriendsUsingANodeId() {
        Iterable<User> fofs = userRepository.getFriendsOfFriends(socialNetworkUniverse.john.getNodeId());
        assertExpectedUserIds(fofs, Arrays.asList("tom001", "susan001", "pam001"));
    }

    @Test
    @Transactional
    public void demoQueryAUsersFriendsOfFriendsUsingAnUserId() {
        Iterable<User> fofs = userRepository.getFriendsOfFriends("john001");
        assertExpectedUserIds(fofs, Arrays.asList("tom001", "susan001", "pam001"));
    }

    private void assertExpectedUserIds(Iterable<User> fofs, List<String> expectedUserIds) {
        Set<String> actualUserIds =  new HashSet<String>();
        for (User fof: fofs) {
            actualUserIds.add(fof.getUserId());
        }
        assertEquals("Unexpected number of fofs", expectedUserIds.size(), actualUserIds.size());
        assertTrue("Invalid users returned", actualUserIds.containsAll(expectedUserIds));
    }
}