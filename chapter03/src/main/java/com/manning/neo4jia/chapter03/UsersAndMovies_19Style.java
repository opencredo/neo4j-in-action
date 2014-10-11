package com.manning.neo4jia.chapter03;

import com.manning.neo4jia.chapter03.relationshiptype.MyRelationshipTypes;
import org.neo4j.graphdb.*;
import org.neo4j.tooling.GlobalGraphOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Aleksa Vukotic
 */
public class UsersAndMovies_19Style {

    private static Logger logger = LoggerFactory.getLogger(UsersAndMovies_19Style.class);

    private final GraphDatabaseService graphDb;

    public Node user1, user2, user3;
    public Node movie1, movie2, movie3;

    public UsersAndMovies_19Style(GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
    }

    public void reset() {
        Transaction tx = graphDb.beginTx();
        try {
            GlobalGraphOperations ggo = GlobalGraphOperations.at(graphDb);
            for (Relationship r : ggo.getAllRelationships()) {
                r.delete();
            }
            for (Node n : ggo.getAllNodes()) {
                n.delete();
            }
            tx.success();
        } catch (Exception e) {   // Note this is not strictly necessary. If an explicit
            tx.failure();         // success has not bee called and the TX fails, finish
            throw e;              // will automatically fail the transaction. Further
            // methods will omit this
        } finally {
            tx.finish();
        }
    }

    public void createGraph() {
        reset();
        createSingleUser();
        createMultipleUsersInSingleTransaction();
        createSimpleRelationshipsBetweenUsers();
        addPropertiesToUserNodes();
        addMorePropertiesToUsers();
        createMoviesNodes();
        addTypePropertiesToNodes();
        addPropertiesToRelationships();

    }

    public void createSingleUser() {
        Transaction tx = graphDb.beginTx();
        try {
            user1 = this.graphDb.createNode();
            logger.info("created user:" + user1.getId());
            tx.success();
        } finally {
            tx.finish();
        }
    }

    public void createMultipleUsersInSingleTransaction() {
        Transaction tx = graphDb.beginTx();
        try {
            user2 = this.graphDb.createNode();
            logger.info("created user:" + user2.getId());
            user3 = this.graphDb.createNode();
            logger.info("created user:" + user3.getId());
            tx.success();
        } finally {
            tx.finish();
        }
    }

    public void addPropertiesToUserNodes() {
        Transaction tx = graphDb.beginTx();
        try {
            user1.setProperty("name", "John Johnson");
            user2.setProperty("name", "Kate Smith");
            user3.setProperty("name", "Jack Jeffries");
            tx.success();
        } finally {
            tx.finish();
        }

    }

    public void addMorePropertiesToUsers() {
        Transaction tx = graphDb.beginTx();
        try {
            user1.setProperty("year_of_birth", 1982);
            user2.setProperty("locked", true);
            user3.setProperty("cars_owned", new String[]{"BMW", "Audi"});
            tx.success();
        } finally {
            tx.finish();
        }

    }


    public void createMoviesNodes() {
        Transaction tx = graphDb.beginTx();
        try {
            movie1 = this.graphDb.createNode();
            movie1.setProperty("name", "Fargo");
            movie2 = this.graphDb.createNode();
            movie2.setProperty("name", "Alien");
            movie3 = this.graphDb.createNode();
            movie3.setProperty("name", "Heat");
            tx.success();
        } finally {
            tx.finish();
        }
    }


    public void addTypePropertiesToNodes() {
        Transaction tx = graphDb.beginTx();
        try {
            user1.setProperty("type", "User");
            user2.setProperty("type", "User");
            user3.setProperty("type", "User");

            movie1.setProperty("type", "Movie");
            movie2.setProperty("type", "Movie");
            movie3.setProperty("type", "Movie");

            tx.success();
        } finally {
            tx.finish();
        }

    }

    public void createSimpleRelationshipsBetweenUsers() {
        Transaction tx = graphDb.beginTx();
        try {
            user1.createRelationshipTo(user2, MyRelationshipTypes.IS_FRIEND_OF);
            user1.createRelationshipTo(user3, MyRelationshipTypes.IS_FRIEND_OF);
            tx.success();
        } finally {
            tx.finish();
        }

    }

    public void addPropertiesToRelationships() {
        Transaction tx = graphDb.beginTx();
        try {
            Relationship rel1 =
                    user1.createRelationshipTo(movie1, MyRelationshipTypes.HAS_SEEN);
            rel1.setProperty("stars", 5);
            Relationship rel2 =
                    user2.createRelationshipTo(movie3, MyRelationshipTypes.HAS_SEEN);
            rel2.setProperty("stars", 3);
            Relationship rel3 =
                    user3.createRelationshipTo(movie1, MyRelationshipTypes.HAS_SEEN);
            rel3.setProperty("stars", 4);
            Relationship rel4 =
                    user3.createRelationshipTo(movie2, MyRelationshipTypes.HAS_SEEN);
            rel4.setProperty("stars", 5);

            tx.success();
        } finally {
            tx.finish();
        }

    }

    public void addLabelToMovies() {
        Label moviesLabel = DynamicLabel.label("MOVIES");


        try (Transaction tx = graphDb.beginTx()) {
            try {
                graphDb.schema().indexFor(DynamicLabel.label("MOVIES")).on("name").create();
            } catch (Exception e) {
                //ignore if index already exist
            }

            tx.success();
        }
        try (Transaction tx = graphDb.beginTx()) {
            // Need a new tx or else you get the following error:
            // Cannot perform data updates in a transaction that has
            // performed schema updates.
            movie1.addLabel(moviesLabel);
            movie2.addLabel(moviesLabel);
            movie3.addLabel(moviesLabel);

            tx.success();
        }

    }

}
