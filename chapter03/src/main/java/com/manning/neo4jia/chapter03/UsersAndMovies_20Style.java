package com.manning.neo4jia.chapter03;

import com.manning.neo4jia.chapter03.relationshiptype.MyRelationshipTypes;
import org.neo4j.graphdb.*;
import org.neo4j.tooling.GlobalGraphOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Aleksa Vukotic
 */
public class UsersAndMovies_20Style {

    private static Logger logger = LoggerFactory.getLogger(UsersAndMovies_20Style.class);

    private final GraphDatabaseService graphDb;

    public Node user1, user2, user3;
    public Node movie1, movie2, movie3;

    public UsersAndMovies_20Style(GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
    }

    public void reset() {
        try (Transaction tx = graphDb.beginTx()) {
            GlobalGraphOperations ggo = GlobalGraphOperations.at(graphDb);
            for (Relationship r : ggo.getAllRelationships()) {
                r.delete();
            }
            for (Node n : ggo.getAllNodes()) {
                n.delete();
            }
            tx.success();
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
        try (Transaction tx = graphDb.beginTx()) {
            user1 = this.graphDb.createNode();
            logger.info("created user:" + user1.getId());
            tx.success();
        }
    }

    public void createMultipleUsersInSingleTransaction() {
        try (Transaction tx = graphDb.beginTx()) {
            user2 = this.graphDb.createNode();
            logger.info("created user:" + user2.getId());
            user3 = this.graphDb.createNode();
            logger.info("created user:" + user3.getId());
            tx.success();
        }
    }

    public void addPropertiesToUserNodes() {
        try (Transaction tx = graphDb.beginTx()) {
            user1.setProperty("name", "John Johnson");
            user2.setProperty("name", "Kate Smith");
            user3.setProperty("name", "Jack Jeffries");
            tx.success();
        }

    }

    public void addMorePropertiesToUsers() {
        try (Transaction tx = graphDb.beginTx()) {
            user1.setProperty("year_of_birth", 1982);
            user2.setProperty("locked", true);
            user3.setProperty("cars_owned", new String[]{"BMW", "Audi"});
            tx.success();
        }

    }


    public void createMoviesNodes() {
        try (Transaction tx = graphDb.beginTx()) {
            movie1 = this.graphDb.createNode();
            movie1.setProperty("name", "Fargo");
            movie2 = this.graphDb.createNode();
            movie2.setProperty("name", "Alien");
            movie3 = this.graphDb.createNode();
            movie3.setProperty("name", "Heat");
            tx.success();
        }
    }


    public void addTypePropertiesToNodes() {
        try (Transaction tx = graphDb.beginTx()) {
            user1.setProperty("type", "User");
            user2.setProperty("type", "User");
            user3.setProperty("type", "User");

            movie1.setProperty("type", "Movie");
            movie2.setProperty("type", "Movie");
            movie3.setProperty("type", "Movie");

            tx.success();
        }

    }

    public void createSimpleRelationshipsBetweenUsers() {
        try (Transaction tx = graphDb.beginTx()) {
            user1.createRelationshipTo(user2, MyRelationshipTypes.IS_FRIEND_OF);
            user1.createRelationshipTo(user3, MyRelationshipTypes.IS_FRIEND_OF);
            tx.success();
        }

    }

    public void addPropertiesToRelationships() {
        try (Transaction tx = graphDb.beginTx()) {
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
