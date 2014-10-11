package com.manning.neo4jia.chapter05;

import org.neo4j.graphdb.RelationshipType;

public class Friend implements RelationshipType{
    public String name() {
        return "friend";
    }
}
