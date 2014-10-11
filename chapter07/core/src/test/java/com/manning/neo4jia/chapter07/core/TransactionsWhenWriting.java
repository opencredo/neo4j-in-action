package com.manning.neo4jia.chapter07.core;


import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;

import static org.junit.Assert.assertEquals;

public class TransactionsWhenWriting extends AbstractTransactionTests{

    @Test
    public void acquireWriteLock() {
        try (Transaction tx = graphDatabaseService.beginTx()) {
            Index<Node> nodeIndex = graphDatabaseService.index().forNodes("byName");
            Node n1 = nodeIndex.get("name", "John").getSingle();
            Node n2 = nodeIndex.get("name", "Bob").getSingle();
            tx.acquireWriteLock(n1);
            tx.acquireWriteLock(n2);
            n1.setProperty("age", 35);
            n2.setProperty("age", 37);
            tx.success();
        }

        int s1, s2;
        try (Transaction tx = graphDatabaseService.beginTx()) {
            Index<Node> nodeIndex = graphDatabaseService.index().forNodes("byName");
            s1 = (Integer) nodeIndex.get("name", "John").getSingle().getProperty("age");
            s2 = (Integer) nodeIndex.get("name", "Bob").getSingle().getProperty("age");
            tx.success();
        }
        assertEquals(35, s1);
        assertEquals(37, s2);
    }

}
