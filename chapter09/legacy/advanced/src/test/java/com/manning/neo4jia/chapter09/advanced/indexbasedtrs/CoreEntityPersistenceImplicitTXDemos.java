package com.manning.neo4jia.chapter09.advanced.indexbasedtrs;

import com.manning.neo4jia.chapter09.advanced.indexbasedtrs.domain.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.graphdb.Transaction;
import org.springframework.data.neo4j.aspects.core.NodeBacked;
import org.springframework.data.neo4j.support.node.Neo4jHelper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * (Via JUnit type tests), this class aims to demonstrate the use
 * and implications for using implicit transactions with an
 * active record type persistence approach.
 *
 * Word of caution:
 *   Implicit transactions are generally discouraged as they tend
 *   to pose more problems than they solve. Without explicit
 *   transactions having been provided - either with @Transactional
 *   or manual TX wrapping, SDN will need to perform reattachment
 *   of detached entities all the time, and having multiple small
 *   implicit transaction can slow your whole system down.
 *
 * Nevertheless - if you want to see how they work, this is the place!
 */
@ContextConfiguration(locations = {"classpath*:/indexbasedtrs/test-advanced-application-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class CoreEntityPersistenceImplicitTXDemos extends CoreEntityPersistenceTestBase {


    @Before
    public void setUp() throws Exception {
        Neo4jHelper.cleanDb(template.getGraphDatabaseService(),true);
    }

    // listing 9.12 (untestable) , for testable variation see
    // demoBasicCreationOfUser()
    @Test
    public void runListing_9_12() {

        User user = new User("john001","John the 1st");
        user.setName("John the 2nd");
        ((NodeBacked)user).persist();
        user.setName("John the 3rd");
        ((NodeBacked)user).persist();

    }


    // (Basic) Testable variation of listing 9.12
    @Test
    public void demoBasicCreationOfUser() {
        // NOTICE the absence of any @Transactional annotations
        //        or explicit TX wrapper code when simply using
        //        the entity and dynamically generated active
        //        record style methods
        User savedUser = createAndAssertBasicCreationOfUser("create-tx-test001", "JoJo");

        // However ... as soon as start engaging with the Neo4j template,
        // we must provide a transaction wrapper. As you can see this can
        // be very fiddly and you need to actually worry about transactions
        // for some aspects of your code - Again you are highly discouraged
        // from using implicit transactions!
        try (Transaction tx = template.getGraphDatabaseService().beginTx()) {
            User loadedUser = template.findOne(((NodeBacked)savedUser).getNodeId(), User.class);
            assertNotNull("user should be able to be loaded" , loadedUser);
            assertEquals("JoJo", loadedUser.getName());
            tx.success();
        }

    }

    // Testable variation of listing 9.12
    @Test
    public void demoUpdatingOfUserName() throws Exception {
        // NOTICE the absence of any @Transactional annotations
        //        or explicit TX wrapper code when simply using
        //        the entity and dynamically generated active
        //        record style methods
        String userId          = "implicit-update-test002";
        String originalName    = "implicit-Original-Name";
        String changedName     = "implicit-Changed-Name";

        User savedUser = createAndAssertBasicCreationOfUser(userId, originalName);

        // Change user's name but don't save/persist the entity yet.
        savedUser.setName(changedName);

        // As with demoBasicCreationOfUser(), as soon as you start
        // engaging with the Neo4j template directly, there is no
        // dynamic aspectj intervention employed at this point and thus
        // no implicit transactions created - Again you are
        // highly discouraged from using implicit transactions!
        try (Transaction tx = template.getGraphDatabaseService().beginTx()) {
            verifyCurrentNameInDB(((NodeBacked)savedUser).getNodeId(), originalName);
            tx.success();
        }

        ((NodeBacked)savedUser).persist();

        // As with demoBasicCreationOfUser(), as soon as you start
        // engaging with the Neo4j template directly, there is no
        // dynamic aspectj intervention employed at this point and thus
        // no implicit transactions created - Again you are
        // highly discouraged from using implicit transactions!
        try (Transaction tx = template.getGraphDatabaseService().beginTx()) {
            verifyCurrentNameInDB(((NodeBacked)savedUser).getNodeId(), changedName);
            tx.success();
        }
    }

}



