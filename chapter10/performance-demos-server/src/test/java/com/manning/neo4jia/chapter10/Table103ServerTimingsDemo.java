package com.manning.neo4jia.chapter10;

import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.rest.graphdb.util.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * Contains the code used to demo the difference in timings when using the
 * embedded vs server options using different "transaction" and batch
 * configurations. These tests form the basis for the results which can be
 * seen in table 10.3.
 *
 * Note: this Junit based Demo class assumes the Neo4j Server is running on
 * localhost port 7474 in a separate process. This is to ensure that the
 * client and server based JVM's are completely separated for the purposes
 * of timing the various different scenarios.
 */
public class Table103ServerTimingsDemo extends AbstractPerformanceTests {

    private static final Logger logger = LoggerFactory.getLogger(Table103ServerTimingsDemo.class);

    /*
    @Test
    public void timeServerModeRawRestAPIOneBatchStreamingOn() {
        timeCreateNodesViaRawApi("ServerModeRawRestAPIIOneTxStreamingOn",
                true, true, 1, NUM_NODES_TO_CREATE);
    }

    @Test
    public void timeServerModeRawRestAPIInSeparateBatchesStreamingOn() {
        timeCreateNodesViaRawApi("ServerModeRawRestAPIInSeparateTxsStreamingOn",
                true, false, NUM_NODES_TO_CREATE, 1);
    }

    @Test
    public void timeServerModeRawRestAPIInMultiBatchesStreamingOn() {
        timeCreateNodesViaRawApi("ServerModeRawRestAPIInBatchTxsStreamingOn",
                true, true, NUM_BATCHES, BATCH_SIZE);
    }
    */

    @Test
    public void timeServerModeRawRestAPIInSeparateBatchesStreamingOff() {
        timeCreateNodesViaRawApi("ServerModeRawRestAPIInSeparateTxsStreamingOff",
                false, false, NUM_NODES_TO_CREATE, 1);
    }

    @Test
    public void timeServerModeRawRestAPIOneBatchStreamingOff() {
        timeCreateNodesViaRawApi("ServerModeRawRestAPIIOneTxStreamingOff",
                false, true, 1, NUM_NODES_TO_CREATE);
    }

    @Test
    public void timeServerModeRawRestAPIInMultiBatchesStreamingOff() {
        timeCreateNodesViaRawApi("ServerModeRawRestAPIInBatchTxsStreamingOff",
                false, true, NUM_BATCHES, BATCH_SIZE);
    }
      /*
    @Test
    public void timeServerModeCypherViaRestAPIOneBatchStreamingOn() {
        timeCreateNodesViaCypher("ServerModeCypherViaRestAPIIOneTxStreamingOn",
                 true, 1, NUM_NODES_TO_CREATE);
    }
    */

    /*
    @Test
    public void timeServerModeCypherViaRestAPIInSeparateBatchesStreamingOn() {
        timeCreateNodesViaCypher("ServerModeCypherViaRestAPIInSeparateTxsStreamingOn",
                 true, NUM_NODES_TO_CREATE, 1);
    }

    @Test
    public void timeServerModeCypherViaRestAPIInMultiBatchesStreamingOn() {
        timeCreateNodesViaCypher("ServerModeCypherViaRestAPIInBatchTxsStreamingOn",
                 true, NUM_BATCHES, BATCH_SIZE);
    }

    @Test
    public void timeServerModeCypherViaRestAPIOneBatchStreamingOff() {
        timeCreateNodesViaCypher("ServerModeCypherViaRestAPIIOneTxStreamingOff",
                false, 1, NUM_NODES_TO_CREATE);
    }

    @Test
    public void timeServerModeCypherViaRestAPIInSeparateBatchesStreamingOff() {
        timeCreateNodesViaCypher("ServerModeCypherViaRestAPIInSeparateTxsStreamingOff",
                false, NUM_NODES_TO_CREATE, 1);
    }

    @Test
    public void timeServerModeCypherViaRestAPIInMultiBatchesStreamingOff() {
        timeCreateNodesViaCypher("ServerModeCypherViaRestAPIInBatchTxsStreamingOff",
                false, NUM_BATCHES, BATCH_SIZE);
    }
    */



    protected void timeCreateNodesViaCypher(String scenario,boolean stream, int numBatches, int numNodes2CreatePerBatch) {

        System.setProperty(Config.CONFIG_STREAM,Boolean.toString(stream));


        int beforeCreationNumNodes = countExistingNodes();
        StopWatch stopWatch = new StopWatch(scenario);
        stopWatch.start();

        for (int a = 0; a < numBatches; a++) {
            Map<String,Object> globalProps = new HashMap<String,Object>();
            List<Map<String,Object>> nodePropList = new ArrayList<>();
            globalProps.put("props",nodePropList);

            for (int i = 0; i < numNodes2CreatePerBatch; i++) {
                Map<String,Object> nodeProps = new HashMap<String,Object>();
                nodeProps.put("name", "User " + i);
                nodePropList.add(nodeProps);
            }
            final String queryString = "CREATE (n { props } ) RETURN n";
            queryEngine.query(queryString, globalProps);
        }
        stopWatch.stop();
        int afterCreationNumNodes = countExistingNodes();
        logger.info("num nodes before creation : " + beforeCreationNumNodes + " , num nodes after creation : " + afterCreationNumNodes);
        assertNumberOfNewNodesCreated(beforeCreationNumNodes, afterCreationNumNodes, numBatches * numNodes2CreatePerBatch);


        logger.info(stopWatch.shortSummary());

    }

    protected void timeCreateNodesViaRawApi(String scenario, boolean stream, boolean batchMode,
                                            int numBatches, int numNodes2CreatePerBatch) {

        System.setProperty(Config.CONFIG_STREAM,Boolean.toString(stream));
        System.setProperty(Config.CONFIG_BATCH_TRANSACTION,Boolean.toString(batchMode));

        assertEquals("Neo4j batchMode not set correctly", batchMode, Config.useBatchTransactions());
        int totalNumNodes = numBatches * numNodes2CreatePerBatch;
        int beforeCreationNumNodes = countExistingNodes();
        StopWatch stopWatch = new StopWatch(scenario);
        stopWatch.start();
        for (int a = 0; a < numBatches; a++) {
            try (Transaction tx = restAPI.beginTx()) {
                for (int i = 0; i < numNodes2CreatePerBatch; i++) {
                    Map<String,Object> props = new HashMap<String,Object>();
                    props.put("name", "User " + i);
                    Node node = restAPI.createNode(props);
                }
                tx.success();
            }
        }
        stopWatch.stop();
        int afterCreationNumNodes = countExistingNodes();
        logger.info("num nodes before creation : " + beforeCreationNumNodes + " , num nodes after creation : " + afterCreationNumNodes);
        long numNodesPerSecs = totalNumNodes / TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTotalTimeMillis());
        logger.info("StopWatch '{}': running time = {} millis ( {} nodes / second ) " ,
                scenario, stopWatch.getTotalTimeMillis(), numNodesPerSecs);
        assertNumberOfNewNodesCreated(beforeCreationNumNodes, afterCreationNumNodes, numBatches * numNodes2CreatePerBatch);

    }

}
