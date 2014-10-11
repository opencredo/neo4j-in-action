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

import java.util.NoSuchElementException;

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
public class RepositoryBasedFinderMethod2Demos {

    @Autowired
    private Neo4jTemplate template;
    @Autowired
    private UserRepository userRepository;
    private SocialNetworkUniverse socialNetworkUniverse;

    public void cleanDb() {
        Neo4jHelper.cleanDb(template.getGraphDatabaseService());
    }

    @Before
    public void setUp() {
        cleanDb();

        try (Transaction tx = template.getGraphDatabaseService().beginTx()) {
            User susan200       = new User("susan200","Susan");
            User susan150       = new User("susan150","Susan");
            User susan300       = new User("susan300","Susan");
            userRepository.save(susan200);
            userRepository.save(susan150);
            userRepository.save(susan300);
            tx.success();
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void demoFindSingleByNameThrowsExceptionWhenMoreAreReturned() {
        try (Transaction tx = template.getGraphDatabaseService().beginTx()) {
            userRepository.findSingleByName("Susan");
            tx.success();
        }
    }

}