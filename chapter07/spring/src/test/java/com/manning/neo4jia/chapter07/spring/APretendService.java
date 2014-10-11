package com.manning.neo4jia.chapter07.spring;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.springframework.transaction.annotation.Transactional;


/**
 *
 */
public class APretendService {

    private GraphDatabaseService gds;

    @Transactional
    public Node createNodeViaAnnotatedMethod(String name, int age)  {
        Node node = gds.createNode();
        node.setProperty("name", name);
        node.setProperty("age", age);
        return node;
    }

    public Node createPlainNode(String name, int age)  {
        Node node = gds.createNode();
        node.setProperty("name", name);
        node.setProperty("age", age);
        return node;
    }


    public void setGraphDatabaseService(GraphDatabaseService gds) {
        this.gds = gds;
    }
}
