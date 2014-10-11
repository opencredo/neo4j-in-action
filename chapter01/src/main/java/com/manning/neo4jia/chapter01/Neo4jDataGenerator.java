package com.manning.neo4jia.chapter01;

import org.apache.commons.lang.RandomStringUtils;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author aleksavukotic
 */
public class Neo4jDataGenerator implements DataGenerator {

    private static Logger logger = LoggerFactory.getLogger(Neo4jDataGenerator.class);
    public static final int BATCH_SIZE = 10000;

    private final GraphDatabaseService graphdb;
    private final List<Long> nodeIds = new ArrayList<Long>();

    public Neo4jDataGenerator(GraphDatabaseService graphdb) {
        this.graphdb = graphdb;
    }

    public void generateUsers(int count, int friendsPerUser) {
        Transaction tx = graphdb.beginTx();
        try {
            for (int i = 0; i < count; i++) {
                Node user = graphdb.createNode();
                user.setProperty("type", "User");
                user.setProperty("name", RandomStringUtils.randomAlphabetic(6));
                nodeIds.add(user.getId());
                if (i % BATCH_SIZE == 0) {
                    tx.success();
                    tx.finish();
                    tx = graphdb.beginTx();
                    logger.info("Commited batch");
                }
            }
            logger.info("Commited batch");
            tx.success();
        } catch (Exception e) {
            tx.failure();
        } finally {
            tx.finish();
        }
        tx = graphdb.beginTx();
        try {
            int relCnt = 0;
            for (Node user : graphdb.getAllNodes()) {
                for (int j = 0; j < friendsPerUser; j++) {
                    user.createRelationshipTo(graphdb.getNodeById(nodeIds.get(getUniformPositiveInt(nodeIds.size()-1) + 1)), Constants.IS_FRIEND_OF);
                    relCnt++;
                    if (relCnt % BATCH_SIZE == 0) {
                        tx.success();
                        tx.finish();
                        tx = graphdb.beginTx();
                        logger.info("Commited batch:"+relCnt);
                    }
                }

            }
            tx.success();
            logger.info("Commited batch:"+relCnt);
        } catch (Exception e) {
            tx.failure();
        } finally {
            tx.finish();
        }

    }

    public static int getUniformPositiveInt(int max) {
        Random random = new Random();

        return Math.abs(random.nextInt(max));
    }


}
