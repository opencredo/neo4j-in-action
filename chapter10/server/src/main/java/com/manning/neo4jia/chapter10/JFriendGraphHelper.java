package com.manning.neo4jia.chapter10;

import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 *
 */
public class JFriendGraphHelper {

    public static RelationshipType IS_FRIEND_OF =  DynamicRelationshipType.withName("IS_FRIEND_OF");

    public static List<String> extractJFriendNamesFromRawAPI(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("Node cannot be null");
        }

        List<String> names = new ArrayList<String>();
        for (Relationship rel: node.getRelationships(IS_FRIEND_OF)) {
            Node friendNode = rel.getOtherNode(node);
            String friendName = (String)friendNode.getProperty("name","unknown");

            if (friendName.startsWith("J")) {
                names.add(friendName);
            }
        }
        return names;
    }


}
