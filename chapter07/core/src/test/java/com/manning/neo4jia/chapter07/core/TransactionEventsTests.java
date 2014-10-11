package com.manning.neo4jia.chapter07.core;

import org.junit.Test;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class TransactionEventsTests extends AbstractTransactionTests {


    @Test
    public void test() {
        final AtomicInteger i = new AtomicInteger(0);
        graphDatabaseService.registerTransactionEventHandler(new TransactionEventHandler<Object>() {
            @Override
            public Object beforeCommit(TransactionData data) throws Exception {
                i.incrementAndGet();
                return null;
            }

            @Override
            public void afterCommit(TransactionData data, Object state) {
                i.incrementAndGet();
            }

            @Override
            public void afterRollback(TransactionData data, Object state) {
                i.incrementAndGet();
            }
        });

        try (Transaction tx = graphDatabaseService.beginTx()) {
            graphDatabaseService.createNode();
            tx.success();
        }

        assertEquals(2, i.intValue());
    }
}
