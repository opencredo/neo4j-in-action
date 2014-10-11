package com.manning.neo4jia.chapter09.simple.util;

import org.neo4j.graphdb.RelationshipType;

public enum SocialNetworkRelationshipType implements RelationshipType {
    IS_FRIEND_OF,
    HAS_SEEN,
    referredBy;
}
