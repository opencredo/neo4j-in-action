package com.manning.neo4jia.chapter04;

import org.neo4j.graphdb.RelationshipType;

/**
 * @author Aleksa Vukotic
 */
public enum MyRelationships implements RelationshipType {
    IS_FRIEND_OF,
    HAS_SEEN
}
