package com.manning.neo4jia.chapter10;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.rest.graphdb.util.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains common code for create X number of nodes for the various server based
 * performance demos/tests used to compare against the embedded mode.
 */
public class AbstractServerVsEmbeddedComparisonTests extends AbstractPerformanceTests {

    private static final Logger logger = LoggerFactory.getLogger(AbstractServerVsEmbeddedComparisonTests.class);

    protected void timeCreateNodes(String scenario, boolean stream, boolean batchMode,
                                 int numBatches, int numNodes2CreatePerBatch) {

        System.setProperty(Config.CONFIG_STREAM,Boolean.toString(stream));
        System.setProperty(Config.CONFIG_BATCH_TRANSACTION,Boolean.toString(batchMode));

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
        assertNumberOfNewNodesCreated(beforeCreationNumNodes, afterCreationNumNodes, numBatches * numNodes2CreatePerBatch);
        logger.info(stopWatch.shortSummary());
    }




}
