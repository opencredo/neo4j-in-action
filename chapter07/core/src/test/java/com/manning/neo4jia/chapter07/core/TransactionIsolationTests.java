package com.manning.neo4jia.chapter07.core;


import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;

public class TransactionIsolationTests extends AbstractTransactionTests {

    private static final Logger logger = LoggerFactory.getLogger(TransactionIsolationTests.class);

    private ExecutorService executorService;

    @Before
    public void setup() {
        super.setup();
        this.executorService = Executors.newFixedThreadPool(2);
    }


    @Test
    @Ignore("This is only applicable to Neo4j 2.0.0-M03 and below where transactions " +
            "were not mandatory for reading")
    public void defaultReadingIsReadCommitted() throws Exception {

        // Ensure we have a starting base
        assertNameAndAgeViaLookup(john.getId(), "John", 34, "John");

        // Do the update in a separate Thread and ensure its actually updated
        doUpdateInSeparateThread(new PersonUpdater("John", 44));

        // Now, back in our thread, ensure we read the commited value
        assertNameAndAgeViaLookup(john.getId(), "John", 44, "John");
    }

    @Test
    public void defaultReadingInsideOfTXIsReadCommitted() throws Exception {
        try (Transaction tx = this.graphDatabaseService.beginTx()) {
            // Ensure we have a starting base
            assertNameAndAgeViaLookup(john.getId(), "John", 34, "John");

            // Do the update in a separate Thread and ensure its actually updated
            doUpdateInSeparateThread(new PersonUpdater("John", 44));

            // Now, back in our thread, ensure we read the commited value
            assertNameAndAgeViaLookup(john.getId(), "John", 44, "John");
            tx.success();
        }
    }

    private void doUpdateInSeparateThread(PersonUpdater personUpdater) throws Exception {
        Future<Integer> confirmationOfUpdatedAge = executorService.submit(personUpdater);
        assertEquals(personUpdater.newAge, confirmationOfUpdatedAge.get());
    }

    private void assertNameAndAgeViaLookup(long personId, String personName, Integer expectedAge, String expectedName) {
        String lookedUpName = lookupNameForId(personId);
        Integer lookedUpAge = lookupAgeForPerson(personName);
        assertEquals(expectedAge, lookedUpAge);
        assertEquals(expectedName, lookedUpName);
    }

    private int lookupAgeForPerson(String name) {
        try (Transaction tx = this.graphDatabaseService.beginTx()) {
            Node n = this.graphDatabaseService.index().forNodes("byName").get("name", name).getSingle();
            int age = (Integer) n.getProperty("age");
            tx.success();
            return age;
        }
    }

    private String lookupNameForId(long id) {
        try (Transaction tx = this.graphDatabaseService.beginTx()) {
            Node n = this.graphDatabaseService.getNodeById(id);
            String name = (String) n.getProperty("name");
            tx.success();
            return name;
        }
    }

    class PersonUpdater implements Callable<Integer> {

        private String personName;
        private Integer newAge;
        private Integer sleepTime;

        PersonUpdater(String personName, Integer newAge) {
            this(personName, newAge, null);
        }

        PersonUpdater(String personName, Integer newAge, Integer sleepTime) {
            this.personName = personName;
            this.newAge = newAge;
            this.sleepTime = sleepTime;
        }

        @Override
        public Integer call() throws Exception {
            try (Transaction tx = graphDatabaseService.beginTx()) {

                Node n = graphDatabaseService.index().forNodes("byName").get("name", personName).getSingle();
                n.setProperty("age", newAge);
                if (sleepTime != null) {
                    Thread.sleep(sleepTime);
                }
                tx.success();
                return newAge;
            }
        }

    }

}
