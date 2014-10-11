package com.manning.neo4jia.chapter07.core;


import org.junit.Test;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.TransactionFailureException;

import static org.junit.Assert.*;

public class NestedTransactionsTests extends AbstractTransactionTests {

    @Test
    public void nestedSuccessfulTransaction() {

        try (Transaction tx = graphDatabaseService.beginTx()) {
            john.setProperty("salary", 10000);
            innerSuccessfulTransaction();
            tx.success();
        }

        try (Transaction readTx = graphDatabaseService.beginTx()) {
            assertTrue(john.getProperty("salary").equals(bob.getProperty("salary")));
            readTx.success();
        }
    }

    @Test(expected = TransactionFailureException.class)
    public void nestedFailedTransaction() {

        try (Transaction tx = graphDatabaseService.beginTx()) {
            john.setProperty("salary", 10000);
            innerFailedTransaction();
            tx.success();
        }

        fail("Should never get here ...");
        try (Transaction tx = graphDatabaseService.beginTx()) {
            assertTrue(john.getProperty("salary").equals(bob.getProperty("salary")));
            tx.success();
        }
    }

    private void innerSuccessfulTransaction() {
        try (Transaction tx = graphDatabaseService.beginTx()) {
            bob.setProperty("salary", 10000);
            tx.success();
        }
    }

    private void innerFailedTransaction() {
        try (Transaction tx = graphDatabaseService.beginTx()) {
            bob.setProperty("salary", 10000);
            tx.failure();
        }
    }
}
