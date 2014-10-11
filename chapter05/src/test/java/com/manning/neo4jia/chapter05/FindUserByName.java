package com.manning.neo4jia.chapter05;


import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.RelationshipIndex;
import org.neo4j.index.lucene.QueryContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FindUserByName {

    private static final Logger logger = LoggerFactory.getLogger(FindUserByName.class);

    GraphDatabaseService graphDatabase;

    String namedUserIndexName = "namesUsers";


    String ageUserIndexName = "ageUsers";

    String friendRelationshipName = "friend";

    String friendsRelationshipIndexName = "friendsIndex";

    @Before
    public void setUp() throws Exception {
//        graphDatabase = new ImpermanentGraphDatabase();
        graphDatabase = new GraphDatabaseFactory().newEmbeddedDatabase("/tmp/neo4j/"+ RandomStringUtils.randomAlphanumeric(5));
    }

    @Test
    public void testIndexing() {
        Transaction tx = graphDatabase.beginTx();
        Index<Node> nameIndex = graphDatabase.index().forNodes(namedUserIndexName);

        Node johnOne = graphDatabase.createNode();
        johnOne.setProperty("name", "john");
        nameIndex.add(johnOne, "name", "john");

        Node johnTwo = graphDatabase.createNode();
        johnTwo.setProperty("name", "john");
        nameIndex.add(johnTwo, "name", "john");


        for (Node node : nameIndex.get("name", "john")) {
            logger.info(node.getProperty("name") + " " + node.getId());
        }
        tx.success();

    }

    @Test
    public void mutlipleHits() {
        Transaction tx = graphDatabase.beginTx();

        Index<Node> nameIndex = graphDatabase.index().forNodes(ageUserIndexName);

        Node john = createUserNodeWithNameAndAgeAndIndex("John", 34);
        Node bob = createUserNodeWithNameAndAgeAndIndex("Bob", 34);
        Node james = createUserNodeWithNameAndAgeAndIndex("James", 29);


        IndexHits<Node> hits = nameIndex.get("age", 34);
        for (Node user : hits) {
            logger.info(""+user.getProperty("name"));
        }
        tx.success();
    }


    @Test
    public void testIndexingRangeQuery() {
        Transaction tx = graphDatabase.beginTx();
        Index<Node> nameIndex = graphDatabase.index().forNodes(namedUserIndexName);

        Node john = createUserNodeWithNameAndAgeAndIndex("John", 34);
        Node bob = createUserNodeWithNameAndAgeAndIndex("Bob", 32);
        Node james = createUserNodeWithNameAndAgeAndIndex("James", 29);

        Index<Node> ageIndex = graphDatabase.index().forNodes(ageUserIndexName);
        IndexHits<Node> results = ageIndex.query(QueryContext.numericRange("age", 30, 39));

        for (Node user : results) {
            logger.info(""+user.getProperty("name"));
        }
        tx.success();
    }


    @Test
    public void basicRelationshipIndexTest() {
        Transaction tx = graphDatabase.beginTx();

        Node john = createUserNodeWithNameAndAgeAndIndex("John", 32);
        Node bob = createUserNodeWithNameAndAgeAndIndex("Bob", 34);
        Relationship friendRel = createFriendFromTo(john, bob);
        RelationshipIndex relIndex = graphDatabase.index().forRelationships(friendRelationshipName);
        relIndex.add(friendRel, "friendOf", "John");

        tx.success();
    }


    protected Relationship createFriendFromTo(Node from, Node to) {
        return from.createRelationshipTo(to, DynamicRelationshipType.withName(friendRelationshipName));
    }


    protected Node createUserNodeWithNameAndAgeAndIndex(String name, int age) {
        Node newUser = graphDatabase.createNode();
        newUser.setProperty("name", name);
        newUser.setProperty("age", age);

        graphDatabase.index().forNodes(namedUserIndexName).add(newUser, "name", name);
        graphDatabase.index().forNodes(ageUserIndexName).add(newUser, "age", age);

        return newUser;
    }


}
