package com.manning.neo4jia.chapter04;

import com.manning.neo4jia.chapter03.UsersAndMovies_20Style;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Aleksa Vukotic
 */
public class TraversalTests {

    private static final Logger logger = LoggerFactory.getLogger(TraversalTests.class);

    static Traversals traversals;
    static GraphDatabaseService graphDb;

    @BeforeClass
    public static void setup() throws IOException {
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase("/tmp/neo4j/" + RandomStringUtils.randomAlphanumeric(5));
        UsersAndMovies_20Style usersAndMovies = new UsersAndMovies_20Style(graphDb);
        usersAndMovies.createGraph();
        traversals = new Traversals(graphDb);
    }

    @Test
    public void listing4_1_findMoviesForUser_iterateThroughAllRelationships() {
        try (Transaction tx = graphDb.beginTx()) {
            Iterable<Node> result = traversals.findMoviesForUser_iterateThroughAllRelationships();
            for (Node movie : result) {
                logger.info("Found movie: " + movie.getProperty("name"));
            }
            tx.success();
        }
    }

    @Test
    public void listing4_2_findMoviesForUser_findRelationshipsByName() {
        try (Transaction tx = graphDb.beginTx()) {
            Iterable<Node> result = traversals.findMoviesForUser_iterateThroughAllRelationships();
            for (Node movie : result) {
                logger.info("Found movie: " + movie.getProperty("name"));
            }
            tx.success();
        }
    }

    @Test
    public void listing4_4_findMoviesThatFriendsLike_memoryIneffient() {

        try (Transaction tx = graphDb.beginTx()) {
            Iterable<Node> result = traversals.findMoviesThatFriendsLike_memoryIneffient();
            for (Node movie : result) {
                logger.info("Found movie: " + movie.getProperty("name"));
            }
            tx.success();
        }
    }


    @Test
    public void listing4_5_findMoviesThatFriendsLike_memoryEfficient() {

        try (Transaction tx = graphDb.beginTx()) {
            Iterable<Node> result = traversals.findMoviesThatFriendsLike_memoryEfficient();
            for (Node movie : result) {
                logger.info("Found movie: " + movie.getProperty("name"));
            }
            tx.success();
        }
    }

    @Test
    public void listing4_6_findMoviesThatMyFriendsLike_traversalFramework() {
        try (Transaction tx = graphDb.beginTx()) {
            Iterable<Node> result = traversals.findMoviesThatMyFriendsLike_traversalFramework();
            for (Node movie : result) {
                logger.info("Found movie: " + movie.getProperty("name"));
            }
            tx.success();
        }
    }

    @Test
    public void listing4_8_findMoviesThatMyFriendsLike_traversalFramework_customEvaluator() {

        try (Transaction tx = graphDb.beginTx()) {
            Iterable<Node> result = traversals.findMoviesThatMyFriendsLike_traversalFramework_customEvaluator();
            for (Node movie : result) {
                logger.info("Found movie: " + movie.getProperty("name"));
            }
            tx.success();
        }
    }


}
