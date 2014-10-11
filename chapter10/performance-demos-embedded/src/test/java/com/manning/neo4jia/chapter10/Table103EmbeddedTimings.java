package com.manning.neo4jia.chapter10;

import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * Contains the code used to demo the difference in timings when using the
 * embedded vs server options using different "transaction" and batch
 * configurations in embedded mode. These tests form the basis for the results
 * which can be seen in table 10.3.
 */
public class Table103EmbeddedTimings {

    private static final Logger logger = LoggerFactory.getLogger(Table103EmbeddedTimings.class);
    protected static final int BATCH_SIZE  = 50000;
    protected static final int NUM_BATCHES =  20;
    protected static final int NUM_NODES_TO_CREATE = BATCH_SIZE * NUM_BATCHES;

    protected GraphDatabaseBuilder graphDbBuilder;
    protected GraphDatabaseService graphDB;
    protected ExecutionEngine engine;

    protected String getDBLocation() {
        return "the-test-embedded-db";
    }

    @Before
    public void setup() throws Exception {
        initEmptyGraph();
        engine = new ExecutionEngine( graphDB );
    }

    @After
    public void teardown() {
        graphDB.shutdown();
    }


    @Test
    public void timeCreateNodesEmbeddedModeInSeparateTxs(){
        timeCreateNodes("EmbeddedModeInSeparateTxs", NUM_NODES_TO_CREATE, 1);
    }

    @Test
    public void timeCreateNodesEmbeddedModeInOneTx() {
        timeCreateNodes("EmbeddedModeInOneTx", 1, NUM_NODES_TO_CREATE);
    }

    @Test
    public void timeCreateNodesEmbeddedModeInMultiBatchedTx() {
        timeCreateNodes("EmbeddedModeInMultiBatchedTx", NUM_BATCHES, BATCH_SIZE);
    }


    private void timeCreateNodes(String scenario, int numTxs, int numNodes2CreatePerTx) {

        int totalNumNodes = numTxs * numNodes2CreatePerTx;
        int beforeCreationNumNodes = countExistingNodes();
        StopWatch stopWatch = new StopWatch(scenario);
        stopWatch.start();
        for (int a = 0; a < numTxs; a++) {
            try (Transaction tx = graphDB.beginTx()) {
                for (int i = 0; i < numNodes2CreatePerTx; i++) {
                    Node node = graphDB.createNode();
                    node.setProperty("name","User " + i);
                }
                tx.success();
            }
        }
        stopWatch.stop();
        long numNodesPerSecs = totalNumNodes /
                TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTotalTimeMillis());
        int afterCreationNumNodes = countExistingNodes();
        logger.info("StopWatch '{}': running time = {} millis ( {} nodes / second ) " ,
                scenario, stopWatch.getTotalTimeMillis(), numNodesPerSecs);
        assertNumberOfNewNodesCreated(beforeCreationNumNodes,afterCreationNumNodes,numTxs * numNodes2CreatePerTx);
    }

    public void initEmptyGraph() throws Exception {
        if (graphDB != null) {
            graphDB.shutdown();
        }
        FileUtils.deleteDirectory(new File(getDBLocation()));
        graphDbBuilder = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(getDBLocation());
        graphDB = graphDbBuilder.newGraphDatabase();
    }

    protected int countExistingNodes() {
        final String queryString = "start n=node(*) return count(n) as total_num_nodes";
        ExecutionResult result = engine.execute(queryString);
        Object val = result.iterator().next().get("total_num_nodes");
        return Integer.parseInt(val.toString());
    }

    protected void assertNumberOfNewNodesCreated(int actualBeforeNum, int actualAfterNum, int expectedNumNodesCreated) {
        assertEquals("Expecting additional nodes to have been created in DB", actualBeforeNum + expectedNumNodesCreated ,actualAfterNum);
    }

}
