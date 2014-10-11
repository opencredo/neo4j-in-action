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
public class UsersAndMovies_20StyleTest {

    static UsersAndMovies_20Style usersAndMovies;
    static GraphDatabaseService graphDb;

    @BeforeClass
    public static void setupClass() throws IOException {
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase("/tmp/neo4j/" + RandomStringUtils.randomAlphanumeric(5));
        usersAndMovies = new UsersAndMovies_20Style(graphDb);
        usersAndMovies.reset();
        usersAndMovies.createMoviesNodes();
    }

    //listing 2.1 code in UsersAndMovies_19StyleTest.java

    @Test
    public void listing3_2_create_single_user() {
        usersAndMovies.createSingleUser();
        try (Transaction tx = graphDb.beginTx()) {
            assertNotNull(graphDb.getNodeById(0));
            tx.success();
        }
    }


    @Test
    public void listing3_3_create_multiple_users() {
        usersAndMovies.createMultipleUsersInSingleTransaction();
        try (Transaction tx = graphDb.beginTx()) {
            assertNotNull(graphDb.getNodeById(1));
            assertNotNull(graphDb.getNodeById(2));
            tx.success();
        }
    }

    @Test
    public void listing3_4_create_realationships_between_users() {
        usersAndMovies.createSimpleRelationshipsBetweenUsers();
        try (Transaction tx = graphDb.beginTx()) {
            assertTrue(usersAndMovies.user1.hasRelationship(MyRelationshipTypes.IS_FRIEND_OF));
            tx.success();
        }
    }

    @Test
    public void listing3_5_add_properties_to_user_nodes() {
        usersAndMovies.addPropertiesToUserNodes();
        try (Transaction tx = graphDb.beginTx()) {
            assertTrue(usersAndMovies.user1.hasProperty("name"));
            assertEquals("John Johnson", usersAndMovies.user1.getProperty("name"));
            tx.success();
        }
    }

    @Test
    public void listing3_6_add_more_properties_to_user_nodes() {
        usersAndMovies.addMorePropertiesToUsers();
        try (Transaction tx = graphDb.beginTx()) {
            assertTrue(usersAndMovies.user1.hasProperty("year_of_birth"));
            assertEquals(1982, usersAndMovies.user1.getProperty("year_of_birth"));
            tx.success();
        }
    }

    @Test
    public void listing3_7_create_movies() {
        try (Transaction tx = graphDb.beginTx()) {
            assertNotNull(graphDb.getNodeById(3));
            assertNotNull(graphDb.getNodeById(4));
            assertNotNull(graphDb.getNodeById(5));
            tx.success();
        }
    }

    @Test
    public void listing3_8_add_type_properties_to_all_nodes() {
        usersAndMovies.addTypePropertiesToNodes();
        try (Transaction tx = graphDb.beginTx()) {
            assertEquals("User", usersAndMovies.user1.getProperty("type"));
            assertEquals("Movie", usersAndMovies.movie1.getProperty("type"));
            tx.success();
        }
    }

    @Test
    public void listing3_9_add_properties_to_relationships() {
        usersAndMovies.addPropertiesToRelationships();
        try (Transaction tx = graphDb.beginTx()) {
            Relationship hasSeen = usersAndMovies.user1.getSingleRelationship(MyRelationshipTypes.HAS_SEEN, Direction.OUTGOING);
            assertEquals(5, hasSeen.getProperty("stars"));
            tx.success();
        }
    }

    @Test
    public void listing3_10_node_labels() {
        usersAndMovies.addLabelToMovies();
        try (Transaction tx = graphDb.beginTx()) {
            ResourceIterable<Node> movies = GlobalGraphOperations.at(graphDb).getAllNodesWithLabel(DynamicLabel.label("MOVIES"));
            assertEquals(3, IteratorUtil.count(movies));
            tx.success();
        }
    }

    @Test
    public void listing3_11_node_labels_and_property() {
        try {
            usersAndMovies.addLabelToMovies();
        } catch (Exception e) {
            //ignore if index already exist
        }
        try (Transaction tx = graphDb.beginTx()) {
            ResourceIterable<Node> movies = graphDb.findNodesByLabelAndProperty(DynamicLabel.label("MOVIES"), "name", "Fargo");
            assertEquals(1, IteratorUtil.count(movies));
            tx.success();
        }
    }


}
