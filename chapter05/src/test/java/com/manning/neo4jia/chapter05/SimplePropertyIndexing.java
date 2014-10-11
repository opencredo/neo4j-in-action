package com.manning.neo4jia.chapter05;


import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;

import java.util.HashSet;
import java.util.Set;

public class SimplePropertyIndexing {


    GraphDatabaseService graphDB;

    String name = "name";

    IndexManager indexManager;

    @Before
    public void setUp() {
//        this.graphDB = new ImpermanentGraphDatabase();
        this.graphDB = new GraphDatabaseFactory().newEmbeddedDatabase("/tmp/neo4j/"+ RandomStringUtils.randomAlphanumeric(5));
        this.indexManager = graphDB.index();
    }

    @Test
    public void testOne() {
        Transaction tx = graphDB.beginTx();
        Node personOne = graphDB.createNode();

        String johnSmithName = "John Smith";
        String johnSmithEmail = "jsmith@example.org";
        personOne.setProperty("name", johnSmithName);
        personOne.setProperty("email", johnSmithEmail);


        Node personTwo = graphDB.createNode();
        personTwo.setProperty(name, "John");

        personOne.createRelationshipTo(personTwo, new Friend());

        IndexManager indexManager = graphDB.index();
        Index<Node> userIndex = indexManager.forNodes("users");
        userIndex.add(personOne, "email", johnSmithEmail);


        tx.success();
    }

    @Test
    public void lookUpFromIndex() {
        testOne();

        String userEmail = "jsmith@example.org";

        IndexManager indexManager = graphDB.index();
        Index<Node> userIndex = indexManager.forNodes("users");
        IndexHits<Node> indexHits = userIndex.get("email", userEmail);
        Node loggedOnUserNode = indexHits.getSingle();

        if (loggedOnUserNode == null) {
            throw new NoSuchUserException("No user with email " + userEmail + " found");
        }

        Set<Node> friends = new HashSet<Node>();

        Iterable<Relationship> outboundFriendRelationships =
                loggedOnUserNode.getRelationships(DynamicRelationshipType.withName("is_friend_of"), Direction.OUTGOING);
        for(Relationship frRel: outboundFriendRelationships){
            friends.add(frRel.getOtherNode(loggedOnUserNode));
        }

    }

    @Test
    public void updateIndex() {
        testOne();

        String userEmail = "jsmith@example.org";
        String updatedUserMail = "john.smith@example.org";

        IndexManager indexManager = graphDB.index();
        Index<Node> userIndex = indexManager.forNodes("users");
        IndexHits<Node> indexHits = userIndex.get("email", userEmail);
        Node loggedOnUserNode = indexHits.getSingle();

        if (loggedOnUserNode == null) {
            throw new NoSuchUserException("No user with email " + userEmail + " found");
        }

        userIndex.remove(loggedOnUserNode,"email", loggedOnUserNode.getProperty("email"));
        loggedOnUserNode.setProperty("email", updatedUserMail);
        userIndex.add(loggedOnUserNode,"email", updatedUserMail);



    }


}
