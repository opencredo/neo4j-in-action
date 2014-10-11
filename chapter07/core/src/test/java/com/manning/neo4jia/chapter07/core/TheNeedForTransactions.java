package com.manning.neo4jia.chapter07.core;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotInTransactionException;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TheNeedForTransactions {

    private static final Logger logger = LoggerFactory.getLogger(TheNeedForTransactions.class);

    private GraphDatabaseService graphDatabaseService;

    @Before
    public void setup() {
        this.graphDatabaseService = new GraphDatabaseFactory().newEmbeddedDatabase("/tmp/neo4j/" + RandomStringUtils.randomAlphanumeric(5));
        try (Transaction tx = this.graphDatabaseService.beginTx()) {
            Node n = this.graphDatabaseService.createNode();
            n.setProperty("name", "John");
            Index<Node> nodeIndex = this.graphDatabaseService.index().forNodes("byName");
            nodeIndex.add(n, "name", "John");
            tx.success();
        }
    }

    @Test(expected = NotInTransactionException.class)
    public void readThenMutateWithNoTransaction() {
        Node userNode = graphDatabaseService.index().forNodes("byName").get("name", "John").getSingle();
        userNode.setProperty("age", 34);

    }

    @Test
    public void readThenMutateWithTransaction() {
        Transaction tx = graphDatabaseService.beginTx();
        try {
            Node userNode = graphDatabaseService.index().forNodes("byName").get("name", "John").getSingle();
            userNode.setProperty("age", 34);
            tx.success();
        } finally {
            tx.finish();
        }
    }

//    @Ignore("Very slow.")
//    @Test(expected = OutOfMemoryError.class)
    @Test
    public void largeTransactionsEatMemory() {
        try (Transaction tx = graphDatabaseService.beginTx()) {
            for (int i = 0; i < 100000000; i++) {
                Node n = this.graphDatabaseService.createNode();
                n.setProperty("number", i);
            }
            tx.success();
        }
    }

}
