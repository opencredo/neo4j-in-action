package com.manning.neo4jia.chapter05;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.helpers.collection.IteratorUtil;

import static org.junit.Assert.assertEquals;

/**
 * @author aleksav
 */
public class SchemaIndexingTests {

    GraphDatabaseService graphDb;

    @Before
    public void setup() {
        this.graphDb = new GraphDatabaseFactory().newEmbeddedDatabase("/tmp/neo4j/" + RandomStringUtils.randomAlphanumeric(5));
    }

    @After
    public void teardown() {
        this.graphDb.shutdown();
    }

    @Test
    public void testSchemaIndex() {
        Label movieLabel = DynamicLabel.label("MOVIE");
        Label userLabel = DynamicLabel.label("USER");
        Node movie, user;

        // Define schema indexes
        try (Transaction tx = graphDb.beginTx()) {
            graphDb.schema().indexFor(movieLabel).on("name").create();
            graphDb.schema().indexFor(userLabel).on("name").create();
            tx.success();
        }

        // Add labels
        try (Transaction tx = graphDb.beginTx()) {
            movie = graphDb.createNode(movieLabel);
            movie.setProperty("name", "Michael Collins");
            user = graphDb.createNode(userLabel);
            user.setProperty("name", "Michael Collins");
            tx.success();
        }

        // Verify
        try (Transaction tx = graphDb.beginTx()) {
            ResourceIterable<Node> result = graphDb.findNodesByLabelAndProperty(movieLabel, "name", "Michael Collins");
            assertEquals(1, IteratorUtil.count(result));
            assertEquals(movie.getId(), result.iterator().next().getId());
            tx.success();
        }
    }

    @Test
    public void testSchemaIndex2() {
        Label userLabel = DynamicLabel.label("USER");
        Label adminLabel = DynamicLabel.label("ADMIN");
        Node user;

        // Define schema indexes
        try (Transaction tx = graphDb.beginTx()) {
            graphDb.schema().indexFor(userLabel).on("name").create();
            graphDb.schema().indexFor(adminLabel).on("name").create();
            tx.success();
        }

        // Add labels
        try (Transaction tx = graphDb.beginTx()) {
            user = graphDb.createNode(userLabel, adminLabel);
            user.setProperty("name", "Peter Smith");
            tx.success();
        }

        // Verify
        ResourceIterable<Node> adminSearch = null;
        ResourceIterable<Node> userSearch  = null;
        try (Transaction tx = graphDb.beginTx()) {
            adminSearch = graphDb.findNodesByLabelAndProperty(adminLabel, "name", "Peter Smith");
            assertEquals(1, IteratorUtil.count(adminSearch));

            userSearch = graphDb.findNodesByLabelAndProperty(userLabel, "name", "Peter Smith");
            assertEquals(1, IteratorUtil.count(userSearch));
            tx.success();
        }

        // Delete User
        try (Transaction tx = graphDb.beginTx()) {
            user.delete();
            tx.success();
        }

        // Verify again
        try (Transaction tx = graphDb.beginTx()) {
            adminSearch = graphDb.findNodesByLabelAndProperty(adminLabel, "name", "Peter Smith");
            assertEquals(0, IteratorUtil.count(adminSearch));

            userSearch = graphDb.findNodesByLabelAndProperty(userLabel, "name", "Peter Smith");
            assertEquals(0, IteratorUtil.count(userSearch));
            tx.success();
        }
    }




}
