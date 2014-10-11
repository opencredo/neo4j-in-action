package com.manning.neo4jia.chapter05;


import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

public class KatesPopulairtyGrowingPerformanceComparison {

    GraphDatabaseService graphDatabase;

    String friendsRelationshipIndexName = "friendsRelationshipIndex";

    String friendsNodeIndexName = "friendsNodeIndex";

    @Before
    public void setUp() throws Exception {
        graphDatabase = new GraphDatabaseFactory().newEmbeddedDatabase("/tmp/neo4j/"+ RandomStringUtils.randomAlphanumeric(5));
//        graphDatabase = new ImpermanentGraphDatabase();
    }

    protected final String friendRelationshipName = "is_friend_of";

    protected Relationship createFriendFromTo(Node from, Node to, boolean indexRelationship) {
        Relationship friend =  from.createRelationshipTo(to, DynamicRelationshipType.withName(friendRelationshipName));
        if(indexRelationship){

        }

        return friend;
    }


    protected Node createUserNodeWithNameAndAgeAndIndex(String name, int age) {
        Node newUser = graphDatabase.createNode();
        newUser.setProperty("name", name);
        newUser.setProperty("age", age);

        return newUser;
    }


}
