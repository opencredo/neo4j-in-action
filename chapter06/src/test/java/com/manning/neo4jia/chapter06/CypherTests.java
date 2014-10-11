package com.manning.neo4jia.chapter06;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Aleksa Vukotic
 */
public class CypherTests {

    private static final Logger logger = LoggerFactory.getLogger(CypherTests.class);
    
    public static final String STORE_DIR = "/tmp/neo4j-chapter06/"+ RandomStringUtils.randomAlphanumeric(10);
    private DataCreator dataCreator;
    private GraphDatabaseService graphDb;

    @Before
    public void setup() throws IOException {
        dataCreator = new DataCreator();
        dataCreator.setStoreDir(STORE_DIR);
        dataCreator.recreateData();

        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(STORE_DIR);
    }

    @After
    public void shutdown(){
        if (graphDb != null) {
            graphDb.shutdown();
        }
    }

    @Test
    public void find_john_has_seen() {
        ExecutionEngine engine = new ExecutionEngine(graphDb);

        String cql = "start user=node:users(name = \"John Johnson\")" +
                        "match (user)-[:HAS_SEEN]->(movie)" +
                        "return movie;";


        ExecutionResult result = engine.execute(cql);
        logger.info("Execution result:" + result.toString());
        for(Map<String,Object> row : result){
            logger.info("Row:" + row);
        }
        List<String> columns = result.columns();
        for(String column : columns){
            logger.info("Column:" + column);
            Iterator<Object> columnValues = result.columnAs(column);
            while(columnValues.hasNext()){
                logger.info("Value:" + columnValues.next());
            }

        }
    }

    @Test
    public void find_movies_johns_friends_have_seen() {
        ExecutionEngine engine = new ExecutionEngine(graphDb);

        String cql = "start john=node:users(name = \"John Johnson\") match john-[:IS_FRIEND_OF]->(user)-[:HAS_SEEN]->(movie) return movie;\n";


        ExecutionResult result = engine.execute(cql);
        logger.info("Execution result:" + result.dumpToString());
    }

    @Test
    //shown for completeness
    //this query does not return expected results - see next test for correct solution
    public void find_movies_johns_friends_have_seen_but_john_hasnt_seen_INCORRECT() {
        ExecutionEngine engine = new ExecutionEngine(graphDb);

        String cql = "start john=node:users(name = \"John Johnson\")\n" +
                "match\n" +
                "john-[:IS_FRIEND_OF]->(user)-[:HAS_SEEN]->(movie),\n" +
                "john-[r:HAS_SEEN]->(movie)\n" +
                "return movie;\n";


        ExecutionResult result = engine.execute(cql);
        logger.info("Execution result:" + result.dumpToString());
    }
    @Test
    public void find_movies_johns_friends_have_seen_but_john_hasnt_seen_CORRECT() {
        ExecutionEngine engine = new ExecutionEngine(graphDb);

        String cql = "start john=node:users(name = \"John Johnson\")\n" +
                "match john-[:IS_FRIEND_OF]->(user)-[:HAS_SEEN]->(movie) \n" +
                "optional match john-[r:HAS_SEEN]->(movie)\n" +
                "where r is null\n" +
                "return movie.name;\n";


        ExecutionResult result = engine.execute(cql);
        logger.info("Execution result:" + result.dumpToString());
    }

    //all other cyphe queries used throughout chapter 6 are listed in the src/test/resources/cypher_plain_text.txt file in this project

}
