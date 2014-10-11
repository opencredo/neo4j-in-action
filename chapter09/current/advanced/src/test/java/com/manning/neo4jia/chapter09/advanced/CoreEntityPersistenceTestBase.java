package com.manning.neo4jia.chapter09.advanced;

import com.manning.neo4jia.chapter09.advanced.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.aspects.core.NodeBacked;
import org.springframework.data.neo4j.conversion.Result;
import org.springframework.data.neo4j.support.Neo4jTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * Base class used to demo core persistence functionality
 */
public class CoreEntityPersistenceTestBase {

    @Autowired
    protected Neo4jTemplate template;

    protected User createAndAssertBasicCreationOfUser(String userId, String name) {
        // 1. Create a new user object
        //    (Note: it is not strictly required to perform a cast on the user object
        //           to a NodeBacked interface - behind the scenes, the aspectj compiler
        //           will automatically do this for you however, to make life easier for
        //           IDE's this can sometimes prove quite useful to prevent them moaning
        //           at you about compilation issues which theoretically wont exists post
        //           aspectj compilation ....)
        User user      = new User(userId,name);
        Long userNodeId = ((NodeBacked)user).getNodeId();
        assertNull("Not expecting user to have been persisted in graph and" +
                   "to have been allocated a node id " +
                   "yet as we have not issued an explicit persist call", userNodeId);

        // 2. Save / Persist it and ensure it is allocated a node id
        ((NodeBacked)user).persist();
        userNodeId = ((NodeBacked)user).getNodeId();
        assertNotNull("Expecting user to have been persisted in graph and " +
                      "allocated a node id now " +
                      "as we have issued an explicit persist call", userNodeId);
        return user;
    }

    protected void verifyCurrentNameInDB(long nodeId, String expectedName) throws Exception {

        ExecutorService executor = Executors.newFixedThreadPool(1);
        SeparatelyScopeTx callable = new SeparatelyScopeTx(nodeId);
        executor.submit(callable);
        String actualName = callable.call();
        assertEquals("Name of user stored in database is not what was expected ",
                expectedName, actualName);
//
//        Result<Map<String, Object>> result =
//                template.query(String.format("MATCH (n) where ID(n)=%s return n.name ",nodeId), new HashMap());
//        assertNotNull("Expected result",result);
//        Map<String, Object> singleResult = result.single();
//        assertNotNull("Expected result to return single entity",singleResult);
//        assertEquals("Name of user stored in database is not what was expected ",
//                expectedName, singleResult.get("n.name"));
    }

    public class SeparatelyScopeTx implements Callable<String> {

        private final long nodeId;

        public SeparatelyScopeTx(long nodeId) {
            this.nodeId = nodeId;
        }
        @Override
        public String call() throws Exception {
            Result<Map<String, Object>> result =
                    template.query(String.format("MATCH (n) where ID(n)=%s return n.name ",nodeId), new HashMap());
            assertNotNull("Expected result",result);
            Map<String, Object> singleResult = result.single();
            assertNotNull("Expected result to return single entity",singleResult);
            return (String)singleResult.get("n.name");
        }

    }

}



