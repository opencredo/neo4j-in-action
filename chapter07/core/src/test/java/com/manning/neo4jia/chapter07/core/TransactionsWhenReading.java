package com.manning.neo4jia.chapter07.core;


import org.apache.commons.lang.RandomStringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;

public class TransactionsWhenReading {

    private GraphDatabaseService graphDatabaseService;

    @Before
    public void setup() {
        this.graphDatabaseService = new GraphDatabaseFactory().newEmbeddedDatabase("/tmp/neo4j/" + RandomStringUtils.randomAlphanumeric(5));
        try (Transaction tx = graphDatabaseService.beginTx()) {
            Node n = this.graphDatabaseService.createNode();
            n.setProperty("name", "John");
            n.setProperty("age", 34);
            Index<Node> nodeIndex = this.graphDatabaseService.index().forNodes("byName");
            nodeIndex.add(n, "name", "John");
            tx.success();
        }
    }


    @Test
    @Ignore("Only works in Neo4j 2.0.0-M03 and below")
    public void readTwiceNoTx() {
        Node n = this.graphDatabaseService.index().forNodes("byName").get("name", "John").getSingle();
        int age = (Integer) n.getProperty("age");
        //do something else
        int secondAge = (Integer) n.getProperty("age");

        Assert.assertEquals(age, secondAge);
    }

    @Test
    public void readTwiceTx() {
        try (Transaction tx = graphDatabaseService.beginTx()) {
            Node n = this.graphDatabaseService.index().forNodes("byName").get("name", "John").getSingle();
            tx.acquireReadLock(n);
            int age = (Integer) n.getProperty("age");
            //do something else
            int secondAge = (Integer) n.getProperty("age");

            Assert.assertEquals(age, secondAge);

            tx.success();
        }
    }
}
