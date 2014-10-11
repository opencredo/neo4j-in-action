package com.manning.neo4jia.chapter07.spring;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotInTransactionException;
import org.neo4j.graphdb.Transaction;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 */
public class SpringTransaction1Test {

    private ClassPathXmlApplicationContext ctx;
    private GraphDatabaseService gds;
    private APretendService pretendService;

    @Before
    public void setUp() throws Exception {
        ctx = new ClassPathXmlApplicationContext("classpath:spring/SpringTransactionTest-context.xml");
        gds = ctx.getBean(GraphDatabaseService.class);
        pretendService = ctx.getBean(APretendService.class);
    }

    @After
    public void tearDown() throws Exception {
        if (ctx != null) ctx.close();
    }

    @Test(expected = NotInTransactionException.class)
    public void testNodeCreationOutsideOfTransactionShouldFail() {
        pretendService.createPlainNode("John", 34);
    }

    @Test
    public void testNodeCreationWithManualTxHandlingAcquiredFromSpringShouldSucceed() throws Exception {

        Node node = null;
        try (Transaction tx = gds.beginTx()) {
            node = pretendService.createPlainNode("John", 34);
            assertNotNull(node);
            tx.success();
        }

        try (Transaction tx = gds.beginTx()) {
            Node readBackInNewTx = gds.getNodeById(node.getId());
            assertEquals(node.getId(), readBackInNewTx.getId());
            assertEquals("John", readBackInNewTx.getProperty("name"));
            assertEquals(34, readBackInNewTx.getProperty("age"));
            tx.success();
        }

    }

    @Test
    public void testNodeCreationWithSpringAnnotatedTxOnServiceMethodShouldSucceed() throws Exception {
        Node node = pretendService.createNodeViaAnnotatedMethod("John", 34);
        assertNotNull(node);
    }


}
