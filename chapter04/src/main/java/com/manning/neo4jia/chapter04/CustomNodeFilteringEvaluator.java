package com.manning.neo4jia.chapter04;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;

/**
 * @author aleksavukotic
 */
public class CustomNodeFilteringEvaluator implements Evaluator {
    private RelationshipType hasSeenRelationshipType = DynamicRelationshipType.withName("HAS_SEEN");
    private final Node userNode;

    public CustomNodeFilteringEvaluator(Node userNode) {
        this.userNode = userNode;
    }

    public Evaluation evaluate(Path path) {
        Node endNode = path.endNode();
        if (!endNode.hasProperty("type") || !endNode.getProperty("type").equals("Movie")) {
            return Evaluation.EXCLUDE_AND_CONTINUE;
        }
        for (Relationship r : endNode.getRelationships(Direction.INCOMING, hasSeenRelationshipType)) {
            if (r.getStartNode().equals(userNode)) {
                return Evaluation.EXCLUDE_AND_CONTINUE;
            }

        }
        return Evaluation.INCLUDE_AND_CONTINUE;
    }
}
