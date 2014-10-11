package com.manning.neo4jia.chapter03.relationshiptype;

import org.neo4j.graphdb.RelationshipType;

/**
 * @author Aleksa Vukotic
 */
public class IsFriendOf implements RelationshipType {

    public static final String IS_FRIEND_OF = "IS_FRIEND_OF";

    public String name() {
        return IS_FRIEND_OF;
    }
}
