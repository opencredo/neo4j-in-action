package com.manning.neo4jia.chapter06;

import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 * @author Aleksa Vukotic
 */
public class CypherExecutor {

    private final GraphDatabaseService graphDb;

    public CypherExecutor(GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
    }

    public ExecutionResult executeCypher(String cql) {
        ExecutionEngine engine = new ExecutionEngine(graphDb);

        ExecutionResult result = engine.execute(cql);
        return result;

    }
}
