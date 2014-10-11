package com.manning.neo4jia.chapter07.core;


import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.DeadlockDetectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LockAndDeadlockTests extends AbstractTransactionTests {

    private static final Logger logger = LoggerFactory.getLogger(LockAndDeadlockTests.class);

    private ExecutorService executorService;

    @Before
    public void setup() {
        super.setup();
        this.executorService = Executors.newFixedThreadPool(2);
    }

    @Test
    @Ignore("This test hangs as the write lock cannot be acquire whilst there is " +
            "a read lock in place and the current code (reading) is waiting for " +
            "the update to occur .... stalemate ")
    public void unrecoverableWriteLockWaitingForReadLockToBeReleased() throws Exception {

        try (Transaction tx = this.graphDatabaseService.beginTx()) {

            // Acquire the lock on John, so that everything we do in here
            // will only be what is visible to us
            tx.acquireReadLock(john);
            assertNameAndAgeViaLookup(john.getId(), "John", 34, "John");

            // Do the update in a separate Thread and ensure its actually updated
            doUpdateInSeparateThread(new PersonUpdater("John", 44));

            // Now, back in our thread, ensure we read the original value
            // (i.e. NOT the value that was committed in the separate thread)
            assertNameAndAgeViaLookup(john.getId(), "John", 34, "John");
            tx.success();
        }

        // But now, outside of our lock and TX, we should be able to read the latest value
        try (Transaction tx = this.graphDatabaseService.beginTx()) {
            assertNameAndAgeViaLookup(john.getId(), "John", 44, "John");
        }
    }

    @Test
    public void writeLockBlockedUntilReadLockIsReleased() throws Exception {

        PersonUpdater personUpdater = new PersonUpdater("John", 44);
        Future<Integer> confirmationOfUpdatedAge;
        try (Transaction tx = this.graphDatabaseService.beginTx()) {

            // Acquire the lock on John, so that everything we do in here
            // will only be what is visible to us
            tx.acquireReadLock(john);
            assertNameAndAgeViaLookup(john.getId(), "John", 34, "John");

            // Do the update in a separate Thread
            // NB - We cannot actually call get on the future as it will block until
            //      the read releases the lock
            confirmationOfUpdatedAge = executorService.submit(personUpdater);

            // Now, back in our thread, ensure we read the original value
            // (i.e. NOT the value that was committed in the separate thread)
            assertNameAndAgeViaLookup(john.getId(), "John", 34, "John");
            tx.success();
        }

        // But now, the lock has been released and we should be able to read the
        // latest value after ensuring the update in the other thread finished.
        try (Transaction tx = this.graphDatabaseService.beginTx()) {
            assertEquals(personUpdater.newAge, confirmationOfUpdatedAge.get());
            assertNameAndAgeViaLookup(john.getId(), "John", 44, "John");
        }
    }

    @Test
    public void deadlockScenarioAvoidedByOrderingUpdates() throws Exception {

        DualPersonUpdater dru1 = new DualPersonUpdater("John", "Bob", 44, 46);
        DualPersonUpdater dru2 = new DualPersonUpdater("John", "Bob", 54, 56);

        Future<Boolean> dru1Done = executorService.submit(dru1);
        sleepABit();
        Future<Boolean> dru2Done = executorService.submit(dru2);
        dru1Done.get();
        dru2Done.get();

    }

    //Can't test this directly as the expected exception is nested
    //@Test(expected = DeadlockDetectedException.class)
    @Test
    public void deadlockScenarioDetectedWithBadlyOrderedUpdates() throws Exception {

        try {
            DualPersonUpdater dru1 = new DualPersonUpdater("John", "Bob", 44, 46);
            DualPersonUpdater dru2 = new DualPersonUpdater("Bob", "John", 56, 54);

            Future<Boolean> dru1Done = executorService.submit(dru1);
            sleepABit();
            Future<Boolean> dru2Done = executorService.submit(dru2);
            dru1Done.get();
            dru2Done.get();
        } catch (ExecutionException e) {
            assertTrue(
                    "Expected Neo4j to detect this deadlock but rather issue was" + e.getMessage(),
                    (e.getCause() instanceof DeadlockDetectedException));
        }

    }

    private void sleepABit() throws InterruptedException {
        Thread.sleep(500);
    }

    private void doUpdateInSeparateThread(PersonUpdater personUpdater) throws Exception {
        try (Transaction tx = this.graphDatabaseService.beginTx()) {
            Future<Integer> confirmationOfUpdatedAge = executorService.submit(personUpdater);
            assertEquals(personUpdater.newAge, confirmationOfUpdatedAge.get());
            tx.success();
        }
    }

    private void assertNameAndAgeViaLookup(long personId, String personName, Integer expectedAge, String expectedName) {
        String lookedUpName = lookupNameForId(personId);
        Integer lookedUpAge = lookupAgeForPerson(personName);
        assertEquals(expectedAge, lookedUpAge);
        assertEquals(expectedName, lookedUpName);
    }

    private int lookupAgeForPerson(String name) {
            Node n = this.graphDatabaseService.index().forNodes("byName").get("name", name).getSingle();
            int age = (Integer) n.getProperty("age");
            return age;
    }

    private String lookupNameForId(long id) {
            Node n = this.graphDatabaseService.getNodeById(id);
            String name = (String) n.getProperty("name");
            return name;
    }

    class DualPersonUpdater implements Callable<Boolean> {

        String person1name;
        String person2name;
        Integer age1;
        Integer age2;

        DualPersonUpdater(String person1name, String person2name, Integer age1, Integer age2) {
            this.person1name = person1name;
            this.person2name = person2name;
            this.age1 = age1;
            this.age2 = age2;
        }

        private void updatePerson(String name, Integer age) {
            try (Transaction tx = graphDatabaseService.beginTx()) {
                Node n = graphDatabaseService.index().forNodes("byName").get("name", name).getSingle();
                n.setProperty("age", age);
                tx.success();
            }
        }


        @Override
        public Boolean call() throws Exception {
            logger.info("Txt begun...");
            try (Transaction tx = graphDatabaseService.beginTx()) {
                logger.info("Starting Person1 update...");
                updatePerson(person1name, age1);
                logger.info("Finished Person1 update ...");
                Thread.sleep(1000);
                logger.info("Starting Person2 update...");
                updatePerson(person2name, age2);
                logger.info("Finished Person2 update ...");
                tx.success();
                return Boolean.TRUE;
            }
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
            Transaction tx = graphDatabaseService.beginTx();
            try {

                Node n = graphDatabaseService.index().forNodes("byName").get("name", personName).getSingle();
                tx.acquireWriteLock(n);
                n.setProperty("age", newAge);
                if (sleepTime != null) {
                    Thread.sleep(sleepTime);
                }
                tx.success();
                return newAge;
            } catch (InterruptedException e) {
                logger.info("Caught InterruptedException  " + e.getMessage());
                throw e;
            } finally {
                tx.finish();
            }
        }

    }

}
