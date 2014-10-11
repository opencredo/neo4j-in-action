package com.manning.neo4jia.chapter05;


import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class FindUserByIndexOrByGraphSearch {

    public AtomicLong userIdCounter;

    public GraphDatabaseService graphDatabaseService;

    public static final Random rand = new Random();

    public static final String emailPropertyName = "userEmail";

    public static final String emailSuffix = "@example.org";

    public static final String userIndexName = "users";

    public int[] userCounts = new int[]{10, 10, 20, 30};// 40, 50, 60, 70, 80, 90, 100, 200, 300, 400, 500, 600, 700, 800, 900, 1000, 2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000, 10000, 20000, 30000, 40000, 50000, 60000, 70000, 80000, 90000, 100000, 200000, 300000, 400000, 500000, 600000, 700000, 800000, 900000, 1000000};

    private static final Logger logger = LoggerFactory.getLogger(FindUserByIndexOrByGraphSearch.class);

    public static final double numberOfFindsPerIteration = 1000;

    @Before
    public void setUp() throws Exception {


    }


    @Test
    public void printUserCounts() {
        for (int i = 0; i < userCounts.length; i++) {
            logger.info("" + userCounts[i]);
        }
    }

    @Test
    public void testWithIndex() throws Exception {
        Map<Integer, Double> averageTimeForUserLookupByCount = new HashMap<Integer, Double>();
        List<Long> creationTime = new ArrayList<Long>();

        for (int i = 0; i < userCounts.length; i++) {
            logger.info("starting creation of user count " + userCounts[i]);
            this.userIdCounter = new AtomicLong();
//            this.graphDatabaseService = new ImpermanentGraphDatabase();
            this.graphDatabaseService = new GraphDatabaseFactory().newEmbeddedDatabase("/tmp/neo4j/" + RandomStringUtils.randomAlphanumeric(5));

            long startOfCreation = System.currentTimeMillis();
            createNewUsersWithIndex(userCounts[i]);
            creationTime.add(System.currentTimeMillis() - startOfCreation);


            logger.info("finished creation of user count " + userCounts[i]);

            double start = System.currentTimeMillis();
            for (int j = 0; j < numberOfFindsPerIteration; j++) {
                findUserNodeWithEmailUsingIndex(getRandomEmail(userCounts[i]));
            }
            double timeTaken = System.currentTimeMillis() - start;
            averageTimeForUserLookupByCount.put(userCounts[i], timeTaken / numberOfFindsPerIteration);
            logger.info("Finds took on average " + (timeTaken / numberOfFindsPerIteration) + " ms");


            this.graphDatabaseService.shutdown();


        }
        printFormatted(averageTimeForUserLookupByCount);
        for (int i = 0; i < userCounts.length; i++) {
            logger.info("" + (double) creationTime.get(i) / (double) userCounts[i]);
        }

    }


    @Test
    public void testWithNoIndex() throws Exception {
        Map<Integer, Double> averageTimeForUserLookupByCount = new HashMap<Integer, Double>();
        List<Long> creationTime = new ArrayList<Long>();

        for (int i = 0; i < userCounts.length; i++) {
            logger.info("starting creation of user count " + userCounts[i]);
            this.userIdCounter = new AtomicLong();
            this.graphDatabaseService = new GraphDatabaseFactory().newEmbeddedDatabase("/tmp/neo4j/" + RandomStringUtils.randomAlphanumeric(5));

            long startOfCreation = System.currentTimeMillis();
            createNewUsers(userCounts[i]);
            creationTime.add(System.currentTimeMillis() - startOfCreation);

            logger.info("finished creation of user count " + userCounts[i]);

            double start = System.currentTimeMillis();
            for (int j = 0; j < numberOfFindsPerIteration; j++) {
                findUserNodeWithEmailUsingDumbSearch(getRandomEmail(userCounts[i]));
            }
            double timeTaken = System.currentTimeMillis() - start;
            averageTimeForUserLookupByCount.put(userCounts[i], timeTaken / numberOfFindsPerIteration);
            logger.info("Finds took on average " + (timeTaken / numberOfFindsPerIteration) + " ms");


            this.graphDatabaseService.shutdown();


        }

        printFormatted(averageTimeForUserLookupByCount);
        for (int i = 0; i < userCounts.length; i++) {
            logger.info("" + (double) creationTime.get(i) / (double) userCounts[i]);
        }
    }


    @Test
    public void spaceTest() throws Exception {
        int userCount = 1000;

        File tempDir = new File(System.getProperty("java.io.tmpdir") + "/spacetest/" + RandomStringUtils.randomAlphanumeric(5));

        File noIndexDir = new File(tempDir, getClass().getSimpleName() + "noIndex");
        noIndexDir.mkdir();
        logger.info(noIndexDir.getPath());

        File withIndexDir = new File(tempDir, getClass().getSimpleName() + "withIndex");
        withIndexDir.mkdir();

        userIdCounter = new AtomicLong(0);

        GraphDatabaseService graphDatabaseServiceWithNoIndex =
                new GraphDatabaseFactory().newEmbeddedDatabase(noIndexDir.getPath());

        this.graphDatabaseService = graphDatabaseServiceWithNoIndex;
        createNewUsers(userCount);
        this.graphDatabaseService.shutdown();

        userIdCounter = new AtomicLong(0);
        GraphDatabaseService graphDatabaseServiceWithIndex =
                new GraphDatabaseFactory().newEmbeddedDatabase(withIndexDir.getPath());
        this.graphDatabaseService = graphDatabaseServiceWithIndex;
        createNewUsersWithIndex(userCount);
        this.graphDatabaseService.shutdown();


    }


    public void printFormatted(Map<Integer, Double> averageTimeForUserLookupByCount) {
        for (int i = 0; i < userCounts.length; i++) {
            logger.info("" + averageTimeForUserLookupByCount.get(userCounts[i]));
        }
    }

    public Node findUserNodeWithEmailUsingDumbSearch(String userEmail) {
        try (Transaction tx = graphDatabaseService.beginTx()) {
            for (Node n : graphDatabaseService.getAllNodes()) {
                if (n.hasProperty(emailPropertyName) && n.getProperty(emailPropertyName).equals(userEmail)) {
                    return n;
                }
            }
            throw new RuntimeException("User not found with email " + userEmail);
        }
    }

    public Node findUserNodeWithEmailUsingIndex(String userEmail) {
        Node n = null;
        try (Transaction tx = graphDatabaseService.beginTx()) {
            Index<Node> userIndex = this.graphDatabaseService.index().forNodes(userIndexName);
            n = userIndex.get(emailPropertyName, userEmail).getSingle();
            if (n == null) {
                throw new RuntimeException("User not found with email " + userEmail);
            }
            tx.success();
        }
        return n;

    }


    public String getRandomEmail(int maxUserCount) {
        int randomUserId = rand.nextInt(maxUserCount);
        return String.valueOf(randomUserId) + emailSuffix;
    }

    public void createNewUsers(int userCount) {
        try (Transaction tx = graphDatabaseService.beginTx()) {
            for (int i = 0; i < userCount; i++) {
                Node n = this.graphDatabaseService.createNode();
                n.setProperty(emailPropertyName, getNextUerEmail());
            }
            tx.success();
        }
    }

    public void createNewUsersWithIndex(int userCount) {
        try (Transaction tx = graphDatabaseService.beginTx()) {
            Index<Node> userIndex = this.graphDatabaseService.index().forNodes(userIndexName);
            for (int i = 0; i < userCount; i++) {
                Node n = this.graphDatabaseService.createNode();
                String userEmail = getNextUerEmail();
                n.setProperty(emailPropertyName, userEmail);
                userIndex.add(n, emailPropertyName, userEmail);
            }
            tx.success();
        }
    }

    public String getNextUerEmail() {
        return String.valueOf(userIdCounter.getAndIncrement()) + emailSuffix;
    }


}
