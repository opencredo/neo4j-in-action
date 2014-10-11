package com.manning.neo4jia.chapter09.simple.domain.dodgy;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.RelationshipEntity;

/**
 * Definition of a Relationship which does not actually exist, or rather the
 * name of the RelationshipType does not exist in an existing Neo4j graph.
 */
@RelationshipEntity(type = "DODGY_REL_DEF")
public class DodgyRelDef {

    @GraphId
    Long nodeId;

}