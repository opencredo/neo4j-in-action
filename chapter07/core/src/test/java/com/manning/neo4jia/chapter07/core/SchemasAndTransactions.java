package com.manning.neo4jia.chapter07.core;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.helpers.collection.IteratorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SchemasAndTransactions {

    private static final Logger logger = LoggerFactory.getLogger(SchemasAndTransactions.class);

    private GraphDatabaseService graphDb;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void setup() {
        this.graphDb = new GraphDatabaseFactory()
                .newEmbeddedDatabase("/tmp/neo4j/" + RandomStringUtils.randomAlphanumeric(5));
    }

    @After
    public void teardown() {
        this.graphDb.shutdown();
    }

    @Test
    public void demoExceptionWhenMixingSchemaAndOtherNeo4jMutationsInSameTx() {
        expectedEx.expect(ConstraintViolationException.class);
        expectedEx.expectMessage("Cannot perform data updates in a transaction that has performed schema updates.");

        Label userLabel = DynamicLabel.label("USER");
        try (Transaction tx = graphDb.beginTx()) {

            // Define schema index
            graphDb.schema().indexFor(userLabel).on("name").create();

            // Create a node with the Label and indexed value
            // this should cause an exception and we should get no further
            Node user = graphDb.createNode(userLabel);

            fail("Previous statement should have resulted in an exception " +
                 "and we should not have gotten to here");
            tx.success();
        }

    }

    // Part of code used in sidebar - "Schemas and transactions"
    @Test
    public void demoSchemaFunctionalityDoneInSeparateTxToOtherNeo4jMutations() {

        Label userLabel = DynamicLabel.label("USER");

        // 1. Do schema related work in one transaction.
        try (Transaction tx = graphDb.beginTx()) {
            graphDb.schema().indexFor(userLabel).on("name").create();
            tx.success();
        }

        // 2. Do other work in a separate transaction.
        Node user = null;
        try (Transaction tx = graphDb.beginTx()) {
            user = graphDb.createNode(userLabel);
            user.setProperty("name", "Michael Collins");
            tx.success();
        }

        // 3. Verify in yet another tx to be sure all is well
        try (Transaction tx = graphDb.beginTx()) {
            ResourceIterable<Node> result =
                    graphDb.findNodesByLabelAndProperty(userLabel, "name", "Michael Collins");
            assertEquals(1, IteratorUtil.count(result));
            assertEquals(user.getId(), result.iterator().next().getId());
            tx.success();
        }

    }


}
