package com.manning.neo4jia.chapter03;

import com.manning.neo4jia.chapter03.relationshiptype.MyRelationshipTypes;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.tooling.GlobalGraphOperations;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Aleksa Vukotic
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UsersAndMovies_19StyleTest {

    static UsersAndMovies_19Style usersAndMovies;
    static GraphDatabaseService graphDb;

    @BeforeClass
    public static void setupClass() throws IOException {
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase("/tmp/neo4j/" + RandomStringUtils.randomAlphanumeric(5));
        usersAndMovies = new UsersAndMovies_19Style(graphDb);
        usersAndMovies.reset();
        usersAndMovies.createMoviesNodes();

    }

    @Test
    public void listing3_1_create_single_user() {
        usersAndMovies.createSingleUser();
        Transaction tx = graphDb.beginTx();
        try {
            assertNotNull(graphDb.getNodeById(0));
            tx.success();
        } catch (Exception e) {   // Note this is not strictly necessary. If an explicit
            tx.failure();         // call to the success() method has not occurred, and the
            throw e;              // TX fails, finish() will automatically fail the transaction.
            // Further methods will omit this
        } finally {
            tx.finish();
        }
    }

    //listing 2.2 code in UsersAndMovies_20StyleTest.java
    @Test
    public void listing3_3_create_multiple_users() {
        usersAndMovies.createMultipleUsersInSingleTransaction();
        Transaction tx = graphDb.beginTx();
        try {
            assertNotNull(graphDb.getNodeById(1));
            assertNotNull(graphDb.getNodeById(2));
            tx.success();
        } finally {
            tx.finish();
        }
    }

    @Test
    public void listing3_4_create_realationships_between_users() {
        usersAndMovies.createSimpleRelationshipsBetweenUsers();
        Transaction tx = graphDb.beginTx();
        try {
            assertTrue(usersAndMovies.user1.hasRelationship(MyRelationshipTypes.IS_FRIEND_OF));
            tx.success();
        } finally {
            tx.finish();
        }
    }

    @Test
    public void listing3_5_add_properties_to_user_nodes() {
        usersAndMovies.addPropertiesToUserNodes();
        Transaction tx = graphDb.beginTx();
        try {
            assertTrue(usersAndMovies.user1.hasProperty("name"));
            assertEquals("John Johnson", usersAndMovies.user1.getProperty("name"));
            tx.success();
        } finally {
            tx.finish();
        }
    }

    @Test
    public void listing3_6_add_more_properties_to_user_nodes() {
        usersAndMovies.addMorePropertiesToUsers();
        Transaction tx = graphDb.beginTx();
        try {
            assertTrue(usersAndMovies.user1.hasProperty("year_of_birth"));
            assertEquals(1982, usersAndMovies.user1.getProperty("year_of_birth"));
            tx.success();
        } finally {
            tx.finish();
        }
    }

    @Test
    public void listing3_7_create_movies() {
        usersAndMovies.createMoviesNodes();
        Transaction tx = graphDb.beginTx();
        try {
            assertNotNull(graphDb.getNodeById(3));
            assertNotNull(graphDb.getNodeById(4));
            assertNotNull(graphDb.getNodeById(5));
            tx.success();
        } finally {
            tx.finish();
        }
    }

    @Test
    public void listing3_8_add_type_properties_to_all_nodes() {
        usersAndMovies.addTypePropertiesToNodes();
        Transaction tx = graphDb.beginTx();
        try {
            assertEquals("User", usersAndMovies.user1.getProperty("type"));
            assertEquals("Movie", usersAndMovies.movie1.getProperty("type"));
            tx.success();
        } finally {
            tx.finish();
        }
    }

    @Test
    public void listing3_9_add_properties_to_relationships() {
        usersAndMovies.addPropertiesToRelationships();
        Transaction tx = graphDb.beginTx();
        try {
            Relationship hasSeen = usersAndMovies.user1.getSingleRelationship(MyRelationshipTypes.HAS_SEEN, Direction.OUTGOING);
            assertEquals(5, hasSeen.getProperty("stars"));
            tx.success();
        } finally {
            tx.finish();
        }
    }

    @Test
    public void listing3_10_node_labels() {
        usersAndMovies.addLabelToMovies();
        Transaction tx = graphDb.beginTx();
        try {
            ResourceIterable<Node> movies = GlobalGraphOperations.at(graphDb).getAllNodesWithLabel(DynamicLabel.label("MOVIES"));
            assertEquals(3, IteratorUtil.count(movies));
            tx.success();
        } finally {
            tx.finish();
        }
    }

    @Test
    public void listing3_11_node_labels_and_property() {
        usersAndMovies.addLabelToMovies();
        Transaction tx = graphDb.beginTx();
        try {
            ResourceIterable<Node> movies = graphDb.findNodesByLabelAndProperty(DynamicLabel.label("MOVIES"), "name", "Fargo");
            assertEquals(1, IteratorUtil.count(movies));
            tx.success();
        } finally {
            tx.finish();
        }
    }
}
