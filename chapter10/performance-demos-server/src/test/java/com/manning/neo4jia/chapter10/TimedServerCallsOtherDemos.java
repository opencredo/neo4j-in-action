package com.manning.neo4jia.chapter10;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.rest.graphdb.batch.CypherResult;
import org.neo4j.rest.graphdb.util.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Contains the code used to execute the time based demos using various different
 * techniques. These are additional tests (as opposed to those specifically used
 * for table 10.4 found in TimedServerCallsTable104Demos) to provide additional insight
 * for those interested.
 *
 * Note: this Junit based Demo class assumes the Neo4j Server is running on
 * localhost port 7474 in a separate process. This is to ensure that the
 * client and server based JVM's are completely separated for the purposes
 * of timing the various different scenarios.
 *
 * Additionally, these tests are meant to be run from the provided bash scripts found
 * under src/test/bash/javarestbinding_adams_friends.sh. This is to better control
 * the stopping and starting of the server in a separate process and also to ensure
 * the calling of these tests occurs from both cold and warm server based scenarios.
 */
public class TimedServerCallsOtherDemos extends AbstractTimedServerCallsTests {

    private static final Logger logger = LoggerFactory.getLogger(TimedServerCallsOtherDemos.class);

    @Before
    public void testSetup() {
        super.testSetup();
        //This should be setup before hande via SeedPerformanceTimedDB
        //getOrCreateAdamAndFriendsWithRawRESTApi();
    }

    // ---------------------------------------------------------------------------------
    // These next 4 for perform the same tests and those in run for table 10.4 however
    // in this case with streaming on
    // ---------------------------------------------------------------------------------

    @Test
    public void timeFindJFriendsUsingHypermediaDrivenRESTAPIStreamingOn() {
        timeFindJFriendsUsingHypermediaDrivenRESTAPI(true);
    }

    @Test
    public void timeFindJFriendsInOneCypherCallStreamingOn() {
        timeFindJFriendsInOneCypherCall(true);
    }

    @Test
    public void timeFindJFriendsViaServerPluginStreamingOn() {
        timeFindJFriendsViaServerPluginCall(true);
    }

    @Test
    public void timeFindJFriendsViaUnmanagedExtStreamingOn()  throws Exception {
        timeFindJFriendsViaUnmanagedExt(true);
    }

    // ---------------------------------------------------------------------------------
    // Below some other variations of calls for experimentation purposes
    // ---------------------------------------------------------------------------------

    @Test
    public void timeFindAllFriendDetailsX1InOneCypherCallStreamingOff() {
        timeFindAllFriendDetailsInOneCypherCall(false,1);
    }
    @Test
    public void timeFindAllFriendDetailsX1InOneCypherCallStreamingOn() {
        timeFindAllFriendDetailsInOneCypherCall(true,1);
    }
    @Test
    @Ignore
    public void timeFindAllFriendDetailsXFactorIncreaseInOneCypherCallStreamingOff() {
        timeFindAllFriendDetailsInOneCypherCall(false,25);
    }
    @Test
    @Ignore
    public void timeFindAllFriendDetailsXFactorIncreaseInOneCypherCallStreamingOn() {
        timeFindAllFriendDetailsInOneCypherCall(true,25);
    }

    protected void timeFindAllFriendDetailsInOneCypherCall(boolean bStreamOn, int factorSizeIncrease) {

        String streamOn = Boolean.toString(bStreamOn);
        System.setProperty(Config.CONFIG_STREAM,streamOn);
        String cypherQuery = getCypherQuery(factorSizeIncrease);

        // Make an initial call to ensure that our timed call does not include
        // any potential time taken to load and store the cypher query into a
        // query cache
        restAPI.query(cypherQuery, dummyNodeLookupProps);

        StopWatch stopWatch = new StopWatch("timeFindAllFriendDetailsInOneCypherCallStreaming-" + streamOn + "-RetunSizeFactorIncrease-" + factorSizeIncrease);
        stopWatch.start();

        // 1 x REST API network call made ...
        CypherResult result = restAPI.query(cypherQuery, adamLookupProps);
        Map<?, ?> resultMap = result.asMap();

        if (resultMap.keySet().contains("message") && resultMap.keySet().contains("exception"))
            fail("Error occured trying to call rest API - " + resultMap.get("message") + " (" + resultMap.get("fullname") + ")");

        List data = extractData(resultMap,factorSizeIncrease,"friend");
        stopWatch.stop();

        assertEquals( "Unexpected num data values returned" , NUM_FRIENDS ,data.size() );
        logger.info(stopWatch.shortSummary());
    }


}
