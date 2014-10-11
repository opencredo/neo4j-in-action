package com.manning.neo4jia.chapter08;

import org.apache.commons.lang.RandomStringUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TraversalOrderingDemo extends AbstractTraversalOrderingHelper {

    private static final Logger logger = LoggerFactory.getLogger(TraversalOrderingDemo.class);

    public TraversalOrderingDemo(GraphDatabaseService graphDb) {
        super(graphDb);
    }


    public static void main(String[] args) {
        TraversalOrderingDemo demo = new TraversalOrderingDemo(new GraphDatabaseFactory().newEmbeddedDatabase("/tmp/neo4j/"+ RandomStringUtils.randomAlphanumeric(5)));

        int maxDepthLevel = 12;
        int childrenPerNode = 3;
        Node root = demo.createGraph(childrenPerNode, maxDepthLevel);

        System.out.println("--------WARMING CACHES------");

        for (int j = 0; j < 5; j++) {
            demo.breadthFirstFindAll(root);
            demo.depthFirstFindAll(root);
        }

        System.out.println("--------MEASURING PERFORMANCE------");

        System.out.print(demo.depthFirstFindOne(root, 3, 1));
        System.out.println(demo.breadthFirstFindOne(root, 3, 1));
        System.out.print(demo.depthFirstFindOne(root, 6, 1));
        System.out.println(demo.breadthFirstFindOne(root, 6, 1));
        System.out.print(demo.depthFirstFindOne(root, 9, 1));
        System.out.println(demo.breadthFirstFindOne(root, 9, 1));
        System.out.print(demo.depthFirstFindOne(root, 12, 1));
        System.out.println(demo.breadthFirstFindOne(root, 12, 1));
        System.out.print(demo.depthFirstFindOne(root, 3, 27));
        System.out.println(demo.breadthFirstFindOne(root, 3, 27));
        System.out.println(demo.depthFirstFindOne(root, 6, 729));
        System.out.println(demo.breadthFirstFindOne(root, 6, 729));
        System.out.print(demo.depthFirstFindOne(root, 9, 19683));
        System.out.print(demo.breadthFirstFindOne(root, 9, 19683));
        System.out.println(demo.depthFirstFindOne(root, 12, 531441));
        System.out.println(demo.breadthFirstFindOne(root, 12, 531441));
    }


}
