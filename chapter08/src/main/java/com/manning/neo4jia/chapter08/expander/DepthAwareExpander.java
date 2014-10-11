package com.manning.neo4jia.chapter08.expander;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.BranchState;

import java.util.*;

public class DepthAwareExpander implements PathExpander{

    private final Map<Integer, List<RelationshipType>> relationshipToDepthMapping;

    public DepthAwareExpander(Map<Integer, List<RelationshipType>> relationshipToDepthMapping) {
        this.relationshipToDepthMapping = relationshipToDepthMapping;
    }


    @Override
    public Iterable<Relationship> expand(Path path, BranchState state) {

        int depth = path.length();
        List<RelationshipType> relationshipTypes = relationshipToDepthMapping.get(depth);
        return path.endNode().getRelationships(relationshipTypes.toArray(new RelationshipType[0]));
    }

    @Override
    public PathExpander reverse() {
        List<Integer> keyset = new ArrayList<Integer>(relationshipToDepthMapping.keySet());
        Map<Integer, List<RelationshipType>> reversed = new HashMap<Integer, List<RelationshipType>>();
        for(int i=0; i < keyset.size(); i++){
            reversed.put(i, relationshipToDepthMapping.get(keyset.get(keyset.size() - i)));
        }
        return new DepthAwareExpander(reversed);
    }

}
