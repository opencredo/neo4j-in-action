package com.manning.neo4jia.chapter08.bidirectional;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.traversal.BidirectionalTraversalDescription;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.kernel.SideSelectorPolicies;
import org.neo4j.kernel.Traversal;
import org.neo4j.kernel.Uniqueness;

import java.util.Iterator;

public class BidirectionalDemo {

    public static final DynamicRelationshipType KNOWS = DynamicRelationshipType.withName("KNOWS");
    private final GraphDatabaseService graphDb;

    public BidirectionalDemo(GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
    }

    public static void main(String[] args) {
        BidirectionalDemo demo = new BidirectionalDemo(new GraphDatabaseFactory().newEmbeddedDatabase("/tmp/neo4j/"+ RandomStringUtils.randomAlphanumeric(5)));
        demo.createData();
        System.out.println("S:" + startUser);
        System.out.println("E:" + endUser);
        boolean b = demo.doTheKnowEachOther(startUser, endUser);
        System.out.println(b);

    }
    private static Node startUser;
    private static Node endUser;
    private void createData() {

        Transaction tx = graphDb.beginTx();

        int nodesCount = 5;
        int friendsPerUser = 5;

//        Node user1 = graphDb.createNode();
//        Node user2 = graphDb.createNode();
//        Node user3 = graphDb.createNode();
//        Node user4 = graphDb.createNode();
//        user1.createRelationshipTo(user2, KNOWS);
//        user2.createRelationshipTo(user3, KNOWS);
//        user3.createRelationshipTo(user4, KNOWS);
        for (int i = 0; i < nodesCount; i++) {
            graphDb.createNode();
        }

        for (int i = 0; i < nodesCount; i++) {
            Node node = graphDb.getNodeById(i);
            for (int j = 0; j < friendsPerUser; j++) {
                Node other = graphDb.getNodeById(RandomUtils.nextInt(nodesCount));
                node.createRelationshipTo(other, KNOWS);
                System.out.println(node.getId() + "-" + other.getId());
            }

        }
        startUser = graphDb.getNodeById(0);
        endUser = graphDb.getNodeById(3);
        tx.success();
        tx.finish();

    }

    public boolean doTheKnowEachOther(Node user1, Node user2) {
        BidirectionalTraversalDescription description = Traversal.bidirectionalTraversal()
                .startSide(Traversal.description().relationships(KNOWS)
                        .uniqueness(Uniqueness.NODE_PATH)
                )
                .endSide(Traversal.description().relationships(KNOWS)
                        .uniqueness(Uniqueness.NODE_PATH)
                )
                .collisionEvaluator(new Evaluator() {
                    @Override
                    public Evaluation evaluate(Path path) {
                        return Evaluation.INCLUDE_AND_CONTINUE;
                    }
                })
                .sideSelector(SideSelectorPolicies.ALTERNATING, 100);

        try (Transaction tx = graphDb.beginTx()) {
            Traverser traverser = description.traverse(user1, user2);
            Iterator<Path> iterator = traverser.iterator();
            boolean hasNext = iterator.hasNext();
            while (iterator.hasNext()) {
                System.out.println(iterator.next());
            }
            return hasNext;
        }
    }
}
