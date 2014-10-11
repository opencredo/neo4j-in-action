package com.manning.neo4jia.chapter09.simple.util;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.springframework.data.neo4j.support.Neo4jTemplate;

import java.util.Iterator;

/**
 * Provides some common graph utility methods.
 */
public class GraphUtil {

    private Neo4jTemplate template;

    public GraphUtil(Neo4jTemplate template) {
        this.template = template;
    }

    public int countOutgoingRelationships(Long nodeId, RelationshipType relType) {
        Node node = template.getNode(nodeId);
        Iterable<Relationship> rels = node.getRelationships(relType, Direction.OUTGOING);
        int count = 0;
        for (Relationship r: rels) {
            count++;
        }
        return count;
    }

    public int count(Iterator iterator) {
        int num = 0;
        while (iterator.hasNext()) {
            iterator.next();
            num++;
        }
        return num;
    }
}
