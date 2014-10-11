package com.manning.neo4jia.chapter10.utils.seeding;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;
import static org.neo4j.helpers.collection.MapUtil.map;


/**
 * Helper classes which are used to seed the test server based class
 * to create "Adam" and his friends, some with names starting with a "J"
 *
 */
public class AdamsAndFriendsDataSeeder {

    private static final Logger logger = LoggerFactory.getLogger(AdamsAndFriendsDataSeeder.class);

    static final int EVERY_X_FRIEND_START_WITH_J = 40;
    protected int numAdamsfriends = 600;


    protected final static String ADAM_NAME = "Adam";
    protected final static String ADAM_USER_ID = "adam001";
    protected Long adamNodeId;
    protected ExecutionEngine engine;

    protected GraphDatabaseService graphDB;
    public AdamsAndFriendsDataSeeder(GraphDatabaseService graphDB, int numAdamsfriends) {
        this.graphDB = graphDB;
        this.numAdamsfriends = numAdamsfriends;
        this.engine = new ExecutionEngine( graphDB );
    }

    public void seed() {
        assertDBIsEmpty();
        createSchemaIndex();
        lookupOrCreateAdamAndFriends();
    }

    private void assertDBIsEmpty() {
       try (Transaction tx = graphDB.beginTx()) {
           final String queryString = "match n return count(n) as total_num_nodes";
           ExecutionResult result = engine.execute(queryString);
           Long numNodes = (Long)result.iterator().next().get("total_num_nodes");
           if (numNodes != 0) {
               throw new RuntimeException(format("Expected DB to be empty but it is not, %d nodes found to exist ",numNodes));
           }
           tx.success();
       }
    }

    private void createSchemaIndex() {
        try (Transaction tx = graphDB.beginTx()) {
            final String queryString = "CREATE CONSTRAINT ON (person:Person) ASSERT person.userId IS UNIQUE";
            ExecutionResult result = engine.execute(queryString );
            tx.success();
        }
    }

    private void lookupOrCreateAdamAndFriends() {
        try (Transaction tx = graphDB.beginTx()) {
            boolean adamExists = doesAdamExist();
            if (adamExists) {
                logger.info("Adam found, assuming DB is already setup - using as is");
            } else {
                logger.info("No Adam exists, creating with his friends");
                createAdamAndFriends();
            }
            tx.success();
        }
    }

    private void createAdamAndFriends() {
        adamNodeId = getOrCreateAdam();
        for (int i=1; i <= numAdamsfriends; i++) {
            String friendName = (i%EVERY_X_FRIEND_START_WITH_J == 0) ? "JFriend " + i :  "NonJFriend " + i;
            createFriendOfAdam(friendName,"friend"+i);
        }
    }

    private Long getOrCreateAdam() {
        final String queryString = "MERGE (adam:Person { name: {name}, userId: {userId} }) return id(adam) as adamId ";
        ExecutionResult result = engine.execute(queryString,
                map("name", ADAM_NAME,
                    "userId", ADAM_USER_ID));
        return ((Long)result.iterator().next().get("adamId"));
    }

    private void createFriendOfAdam(String name,String userId) {
        final String queryString =
              "MATCH (adam:Person { userId: { adamUserId } } ) " +
              "CREATE UNIQUE (adam) -[:IS_FRIEND_OF]-> (p:Person { name: {friendName}, userId: {friendUserId} }) " +
              "RETURN p";
        engine.execute(queryString,
                map("adamUserId", ADAM_USER_ID,
                    "friendName", name,
                    "friendUserId", userId));
    }

    private boolean doesAdamExist() {
        final String queryString = "match (adam:Person { userId: {userId} }) return count(adam) as total_num_nodes";
        ExecutionResult result = engine.execute(queryString, map("userId", ADAM_USER_ID) );
        Object val = result.iterator().next().get("total_num_nodes");
        return Integer.parseInt(val.toString()) > 0;
    }


    public Long getAdamNodeId() {
        return adamNodeId;
    }
}
