package com.manning.neo4jia.chapter05.auto;


import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.AutoIndexer;
import org.neo4j.graphdb.index.IndexHits;

import java.util.Iterator;

public class FindUserByDob {

    private final GraphDatabaseService graphDb;

    public FindUserByDob(GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
    }

    public Iterator<Node> getUserByName(String name) {
        AutoIndexer<Node> nodeAutoIndexer = this.graphDb.index().getNodeAutoIndexer();
        IndexHits<Node> nodesWithMatchingName = nodeAutoIndexer.getAutoIndex().get("name", name);
        return nodesWithMatchingName.iterator();
    }

}
