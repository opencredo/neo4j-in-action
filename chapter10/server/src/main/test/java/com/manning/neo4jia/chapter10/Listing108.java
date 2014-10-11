package com.manning.neo4jia.chapter10;

import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.rest.graphdb.RestGraphDatabase;
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sample code for using the java-rest-binding library in order to
 * speak to the Neo4j Server.
 * https://github.com/neo4j/java-rest-binding
 */
public class Listing108 {

    private static final Logger logger = LoggerFactory.getLogger(Listing108.class);
    private static final Label PERSON = DynamicLabel.label("Person");

    @Test
    public void demoUseOfRestGraphDatabaseDirectly() {

        // All calls to the database are done via the REST api under the covers.
        // Note: however that this can cause quite a chatty application
        GraphDatabaseService database
                = new RestGraphDatabase("http://localhost:7474/db/data");
        Node adam = database.findNodesByLabelAndProperty(
                PERSON, "userId", "adam001")
                .iterator().next();
        Iterable<Relationship> adamRelationships = adam.getRelationships();
        for (Relationship rel: adamRelationships) {
            logger.info("Navigating Adams Relationship, found type " +
                    rel.getType().name());
        }
    }

    @Test
    public void demoUseOfCypherViaRestGraphDatabase() {

        // All calls to the database are done via the REST api under the covers.
        // Note: however that this can cause quite a chatty application
        GraphDatabaseService database
                = new RestGraphDatabase("http://localhost:7474/db/data");

        RestCypherQueryEngine queryEngine = new RestCypherQueryEngine(
                ((RestGraphDatabase)database).getRestAPI());

        Node adam = database.findNodesByLabelAndProperty(
                    PERSON, "userId", "adam001")
                    .iterator().next();
        Iterable<Relationship> adamRelationships = adam.getRelationships();
        for (Relationship rel: adamRelationships) {
            logger.info("Navigating Adams Relationship, found type " +
            rel.getType().name());
        }
    }

}
