package com.manning.neo4jia.chapter03.relationshiptype;

import org.neo4j.graphdb.RelationshipType;

/**
 * @author Aleksa Vukotic
 */
public enum MyRelationshipTypes implements RelationshipType {
    IS_FRIEND_OF,
    HAS_SEEN;
}
