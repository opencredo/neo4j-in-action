package com.manning.neo4jia.chapter10.utils.seeding;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains the code used to demo the difference in timings when using the
 * embedded vs server options using different "transaction" and batch
 * configurations in embedded mode. These tests form the basis for the results
 * which can be seen in table 10.3.
 *
 */
public class StreamingNodesSeeder {

    private static final Logger logger = LoggerFactory.getLogger(StreamingNodesSeeder.class);
    private int numStreamingNodes2Create = 120000;

    protected GraphDatabaseService graphDB;
    public StreamingNodesSeeder(GraphDatabaseService graphDB, int numStreamingNodes2Create) {
        this.graphDB = graphDB;
        this.numStreamingNodes2Create = numStreamingNodes2Create;
    }

    public void seed() {
        timeCreateNodes(numStreamingNodes2Create, 1000);
    }

    private void timeCreateNodes(int numNodes2Create, int commitEveryXNodes) {
        Transaction tx = graphDB.beginTx();
        try {
            for (int a = 0; a < numNodes2Create; a++) {
                Node node = graphDB.createNode();
                node.setProperty("name","User " + a);
                node.setProperty("address",  a + " Arb Street ");
                node.setProperty("telephone",  a + "-555-" + a);

                if (a % commitEveryXNodes == 0) {
                    tx.success();
                    tx = graphDB.beginTx();
                    logger.info("Finished batch " + a);
                }
            }
        } finally {
            tx.success();
        }
    }



}
