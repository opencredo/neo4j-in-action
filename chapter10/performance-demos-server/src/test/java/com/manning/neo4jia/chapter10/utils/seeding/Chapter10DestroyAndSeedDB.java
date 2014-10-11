package com.manning.neo4jia.chapter10.utils.seeding;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileSystemUtils;


import java.io.File;


/**
 * Helper class which creates and seeds the performance database.
 */
public class Chapter10DestroyAndSeedDB {

    private static final Logger logger = LoggerFactory.getLogger(Chapter10DestroyAndSeedDB.class);

    protected GraphDatabaseService graphDB;
    protected ExecutionEngine engine;

    // From Neo4j 2.0 onwards the reference node does not exist
    protected int NUM_REFERENCE_NODES = 0;
    protected int EXPECTED_ADAM_NODE_ID = 0;


    protected int NUM_ADAM_NODES = 1;
    protected int NUM_ADAMS_FRIENDS = 600;
    protected int NUM_STREAMING_NODES = 120000;
    protected int TOTAL_NUM_NODES_EXPECTED = NUM_REFERENCE_NODES + NUM_ADAM_NODES + NUM_ADAMS_FRIENDS + NUM_STREAMING_NODES;

    protected Chapter10DestroyAndSeedDB(String dbLocation) {
        graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(dbLocation);
        registerShutdownHook( graphDB );
        engine = new ExecutionEngine( graphDB );
    }

    private void registerShutdownHook(final GraphDatabaseService graphDB) {
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                graphDB.shutdown();
            }
        } );

    }

    public static void main(String[] args) {
        String serverDBLocation = null;
        switch (args.length) {
            case 0 : serverDBLocation = "the-test-server-db";
                     break;
            case 1 : serverDBLocation = args[0];
                     break;
            default : throw new RuntimeException("Unexpected arguments passed to main method : args = " + args );
        }

        deleteDB(serverDBLocation);
        Chapter10DestroyAndSeedDB dbSeeder = new Chapter10DestroyAndSeedDB(serverDBLocation);
        dbSeeder.seed();
    }

    private void seed() {
        int beforeSeeding = countExistingNodes();
        createAdamAndJFriends();
        createExtraNodesForStreamingComparison();
        int afterSeeding = countExistingNodes();

        assertNumberOfNewNodesCreated(beforeSeeding,afterSeeding,TOTAL_NUM_NODES_EXPECTED);

    }

    private void createAdamAndJFriends() {
        AdamsAndFriendsDataSeeder seeder = new AdamsAndFriendsDataSeeder(graphDB,NUM_ADAMS_FRIENDS);
        seeder.seed();

        if (seeder.getAdamNodeId() != EXPECTED_ADAM_NODE_ID) {
            throw new RuntimeException("Adam is currently expected to be created as the first node, he is not he is " + seeder.getAdamNodeId());
        }

   }

    private void createExtraNodesForStreamingComparison() {
        StreamingNodesSeeder seeder = new StreamingNodesSeeder(graphDB,NUM_STREAMING_NODES);
        seeder.seed();
    }

    private static void deleteDB(String serverDBLocation) {
        File database = new File(serverDBLocation);
        logger.info("About to delete neo4j database at serverDBLocation: " + serverDBLocation);
        logger.info("About to delete neo4j database at " + database.getAbsolutePath());
        FileSystemUtils.deleteRecursively(database);
    }

    protected int countExistingNodes() {
        final String queryString = "match n return count(n) as total_num_nodes";
        ExecutionResult result = engine.execute(queryString);
        Object val = result.iterator().next().get("total_num_nodes");
        return Integer.parseInt(val.toString());
    }

    protected void assertNumberOfNewNodesCreated(int actualBeforeNum, int actualAfterNum, int expectedNumNodesCreated) {
        if ( actualBeforeNum + actualAfterNum != (expectedNumNodesCreated + NUM_REFERENCE_NODES))
            throw new RuntimeException("Database not created as expected " +
            "\nactualBeforeNum = " + actualBeforeNum  +
            "\nactualAfterNum = " + actualAfterNum  +
            "\nexpectedNumNodesCreated = " + expectedNumNodesCreated);
    }


}
