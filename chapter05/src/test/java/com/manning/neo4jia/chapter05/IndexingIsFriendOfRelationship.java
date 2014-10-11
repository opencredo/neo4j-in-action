package com.manning.neo4jia.chapter05;


import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;

import java.util.HashSet;
import java.util.Set;

public class IndexingIsFriendOfRelationship {


    GraphDatabaseService graphDatabase;
    String friendsRelationshipIndexName = "friendsRelationshipIndex";


    @Before
    public void setUp() throws Exception {
//        graphDatabase = new ImpermanentGraphDatabase();
        graphDatabase = new GraphDatabaseFactory().newEmbeddedDatabase("/tmp/neo4j/"+ RandomStringUtils.randomAlphanumeric(5));
    }

    @Test
    public void insertRelationshipIntoIndex() {
        Transaction tx = this.graphDatabase.beginTx();

        Node kate = createUserNodeWithEmailAndAge("ksmith@example.org", 27);
        Node john = createUserNodeWithEmailAndAge("jjohnson@example.org", 34);

        Relationship friendRelationship = createFriendFromTo(kate, john);

        Set<Node> friends = new HashSet<Node>();
        Index<Relationship> friendRelationships =
                graphDatabase.index().forRelationships("friendsRelationshipIndex");
        IndexHits<Relationship> friendOfRelationships =
                friendRelationships.get("email", "ksmith@example.org");
        for (Relationship friendRel : friendOfRelationships) {
            friends.add(friendRel.getEndNode());
        }

        tx.success();
        tx.finish();
    }


    protected Relationship createFriendFromTo(Node from, Node to) {
        Relationship friendRelationship =
                from.createRelationshipTo(to, DynamicRelationshipType.withName("is_friend_of"));
        Index<Relationship> friendRelationships =
                graphDatabase.index().forRelationships("friendsRelationshipIndex");

        friendRelationships.add(friendRelationship, "email", "ksmith@example.org");

        return friendRelationship;


    }


    protected Node createUserNodeWithEmailAndAge(String email, int age) {
        Node newUser = graphDatabase.createNode();
        newUser.setProperty("email", email);
        newUser.setProperty("age", age);
        return newUser;
    }


}
