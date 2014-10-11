package com.manning.neo4jia.chapter01;

import org.neo4j.graphdb.DynamicRelationshipType;

/**
 * @author Aleksa Vukotic
 */
public class Constants {
    public static final DynamicRelationshipType IS_FRIEND_OF = DynamicRelationshipType.withName("IS_FRIEND_OF");

}
