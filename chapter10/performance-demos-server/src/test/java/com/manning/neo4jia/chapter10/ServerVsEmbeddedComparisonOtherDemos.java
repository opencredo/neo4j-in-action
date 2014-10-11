package com.manning.neo4jia.chapter10;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Contains the code used to demo the difference in timings when using the
 * embedded vs server options using various different. These are additional
 * tests (as opposed to those specifically used for table 10.3 found in
 * ServerVsEmbeddedComparisonTable103Demos) to provide additional insight
 * for those interested.
 *
 * Note: this Junit based Demo class assumes the Neo4j Server is running on
 * localhost port 7474 in a separate process. This is to ensure that the
 * client and server based JVM's are completely separated for the purposes
 * of timing the various different scenarios.
 */
public class ServerVsEmbeddedComparisonOtherDemos extends AbstractServerVsEmbeddedComparisonTests {

    @Test
    @Ignore // This test just hangs!
    public void timeServerModeRawRestAPIOneBatchStreamingOn() {
        timeCreateNodes("ServerModeRawRestAPIOneTxStreamingOn",
                true, true, 1, NUM_NODES_TO_CREATE);
    }

    @Test
    public void timeServerModeRawRestAPIInSeparateBatchesStreamingOn() {
        timeCreateNodes("ServerModeRawRestAPIInSeparateTxsStreamingOn",
                true, false, NUM_NODES_TO_CREATE, 1);
    }

    @Test
    public void timeServerModeRawRestAPIInMultiBatchesStreamingOn() {
        timeCreateNodes("ServerModeRawRestAPIInBatchTxsStreamingOn",
                true, true, NUM_BATCHES, BATCH_SIZE);
    }


}
