package com.manning.neo4jia.chapter10;

import org.junit.Before;
import org.junit.BeforeClass;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.rest.graphdb.RestAPI;
import org.neo4j.rest.graphdb.RestAPIFacade;
import org.neo4j.rest.graphdb.query.QueryEngine;
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;


/**
 * Contains common setup for the various server based performance demos/tests.
 */
public abstract class AbstractPerformanceTests {

    private static final Logger logger = LoggerFactory.getLogger(AbstractPerformanceTests.class);

    protected static final int BATCH_SIZE  = 50000;
    protected static final int NUM_BATCHES =  20;
    protected static final int NUM_NODES_TO_CREATE = BATCH_SIZE * NUM_BATCHES;

    static final String serverBaseUrl = "http://localhost:7474/db/data";
    protected static RestAPI restAPI;
    protected QueryEngine queryEngine;

    @BeforeClass
    public static void  setup() throws Throwable {
        restAPI = new RestAPIFacade(serverBaseUrl);
        validateServerIsUp();
    }

    private static void validateServerIsUp() throws Throwable {
        try {
            restAPI.getAllLabelNames();
        } catch (Throwable e) {
            logger.error(
                    " !!!!!!!!!!!!!!!! NOTE !!!!!!!!!!!!!!!!!!!!!!!!  \n" +
                            "this test assumes a Neo4j Server is running in a separate process \n" +
                            "on localhost port 7474. You will need to manually start it before \n" +
                            "running these demo tests.");
            throw e;
        }
    }

    protected int countExistingNodes() {
        final String queryString = "match n return count(n) as total_num_nodes";
        final Collection<Integer> result =  IteratorUtil.asCollection(queryEngine.query(queryString, new HashMap<String, Object>()).to(Integer.class));
        return result.iterator().next().intValue();
    }

    @Before
    public void testSetup() {
        queryEngine = new RestCypherQueryEngine(restAPI);
    }

    protected void assertNumberOfNewNodesCreated(int actualBeforeNum, int actualAfterNum, int expectedNumNodesCreated) {
        assertEquals("Expecting additional nodes to have been created in DB", actualBeforeNum + expectedNumNodesCreated ,actualAfterNum);
    }



}
