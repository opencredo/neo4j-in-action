package com.manning.neo4jia.chapter09.advanced;

import com.manning.neo4jia.chapter09.advanced.domain.User;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.graphdb.Transaction;
import org.springframework.data.neo4j.aspects.core.NodeBacked;
import org.springframework.data.neo4j.support.node.Neo4jHelper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertNotNull;

/**
 * (Via JUnit type tests), this class aims to demonstrate the (recommended)
 * use of a standard active record type persistence approach, specifically
 * where the transactions are explicitly being handled by Spring
 * (in contrast to the implicit transaction approach which is generally
 * discouraged)
 */
@ContextConfiguration(locations = {"classpath*:/test-advanced-application-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(defaultRollback = false)
public class CoreEntityPersistenceDemos  extends CoreEntityPersistenceTestBase {

    long precreatedNodeId            = -1L;
    String precreatedUserId          = "precreated-test003";
    String precreatedOriginalName    = "precreated-Original-Name";


    @BeforeTransaction
    public void cleanDb() {
        Neo4jHelper.cleanDb(template.getGraphDatabaseService(),true);
        try (Transaction tx = template.getGraphDatabaseService().beginTx()) {
            User user = createAndAssertBasicCreationOfUser(precreatedUserId, precreatedOriginalName);
            precreatedNodeId = ((NodeBacked)user).getNodeId();
            tx.success();
        }
    }

    // NOTICE : @transactional annotation in contrast to version
    //          defined in CoreEntityPersistenceImplicitTXDemos
    @Test
    @Transactional
    public void demoBasicCreationOfUser() {
        User savedUser = createAndAssertBasicCreationOfUser("create-tx-test001", "JoJo");
        User loadedUser = template.findOne(((NodeBacked)savedUser).getNodeId(), User.class);
        assertNotNull("user should be able to be loaded" , loadedUser);
    }
    
}



