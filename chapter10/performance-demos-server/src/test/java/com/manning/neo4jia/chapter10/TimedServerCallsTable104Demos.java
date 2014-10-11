package com.manning.neo4jia.chapter10;

import org.junit.Before;
import org.junit.Test;

/**
 * Contains the code used to execute the time based demos involving finding
 * all of Adams friends whose names start with a "J" using various different
 * techniques. These tests form the basis for the results which can be
 * seen in table 10.4.
 *
 * Note: this Junit based Demo class assumes the Neo4j Server is running on
 * localhost port 7474 in a separate process. This is to ensure that the
 * client and server based JVM's are completely separated for the purposes
 * of timing the various different scenarios.
 *
 * Additionally, these specific tests are meant to be run from the provided bash scripts found
 * under src/test/bash (i.e. javarestbinding_adams_friends.sh). This is to better control
 * the stopping and starting of the server in a separate process and also to ensure
 * the calling of these tests occurs from both cold and warm server based scenarios.
 */
public class TimedServerCallsTable104Demos extends AbstractTimedServerCallsTests {

    @Before
    public void testSetup() {
        super.testSetup();
    }

    @Test
    public void timeFindJFriendsUsingHypermediaDrivenRESTAPIStreamingOff() {
        timeFindJFriendsUsingHypermediaDrivenRESTAPI(false);
    }

    @Test
    public void timeFindJFriendsInOneCypherCallStreamingOff() {
        timeFindJFriendsInOneCypherCall(false);
    }

    @Test
    public void timeFindJFriendsViaServerPluginStreamingOff() {
        timeFindJFriendsViaServerPluginCall(false);
    }

    @Test
    public void timeFindJFriendsViaUnmanagedExtStreamingOff() throws Exception {
        timeFindJFriendsViaUnmanagedExt(false);
    }


}
