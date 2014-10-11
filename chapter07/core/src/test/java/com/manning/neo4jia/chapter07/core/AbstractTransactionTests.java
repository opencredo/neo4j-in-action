package com.manning.neo4jia.chapter07.core;


import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTransactionTests {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTransactionTests.class);

    protected GraphDatabaseService graphDatabaseService;

    protected Node john;
    protected Node bob;
    protected Integer johnStartingAge = 34;
    protected Integer bobStartingAge = 36;


    @Before
    public void setup() {

        this.graphDatabaseService = new GraphDatabaseFactory().newEmbeddedDatabase("/tmp/neo4j/" + RandomStringUtils.randomAlphanumeric(5));
        try (Transaction tx = this.graphDatabaseService.beginTx()) {
            Index<Node> nodeIndex = this.graphDatabaseService.index().forNodes("byName");

            john = this.graphDatabaseService.createNode();
            john.setProperty("name", "John");
            john.setProperty("age", johnStartingAge);
            nodeIndex.add(john, "name", "John");

            bob = this.graphDatabaseService.createNode();
            bob.setProperty("name", "Bob");
            bob.setProperty("age", bobStartingAge);
            nodeIndex.add(bob, "name", "Bob");

            john.createRelationshipTo(bob, DynamicRelationshipType.withName("KNOWS"));

            tx.success();
        }

    }

    @After
    public void tearDown() {
        this.graphDatabaseService.shutdown();
    }


}
