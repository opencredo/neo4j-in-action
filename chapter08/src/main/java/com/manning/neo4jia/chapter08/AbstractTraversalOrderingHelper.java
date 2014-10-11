package com.manning.neo4jia.chapter08;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.helpers.Pair;
import org.neo4j.kernel.Traversal;
import org.neo4j.kernel.Uniqueness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

abstract class AbstractTraversalOrderingHelper {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTraversalOrderingHelper.class);


    public static final DynamicRelationshipType CHILD = DynamicRelationshipType.withName("CHILD");
    public static final String LEVEL_ID = "level_id";
    public static final String NODE_ID = "node_id";

    private final GraphDatabaseService graphDb;

    public AbstractTraversalOrderingHelper(GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
    }


    private List<Pair<Long, Long>> relationshipsToCreate = new ArrayList<Pair<Long, Long>>();
    Map<Integer, Integer> ids = new HashMap<Integer, Integer>();
    int nodecnt = 0;
    Transaction tx = null;
    public Node createNodeWithChildren(int childrenPerNode, int nodeDepthLevel, int maxDepthLevel) {

        Node node = graphDb.createNode();
        node.setProperty("depthLevel", nodeDepthLevel);
        node.setProperty(NODE_ID, nodecnt);
        nodecnt++;
        Integer id = this.ids.get(nodeDepthLevel);
        if (id == null) {
            id = 0;
        }
        id++;
        this.ids.put(nodeDepthLevel, id);
        node.setProperty(LEVEL_ID, id);

        if (nodeDepthLevel == maxDepthLevel) {
            return node;
        }

        for (int i = 0; i < childrenPerNode; i++) {
            Node child = createNodeWithChildren(childrenPerNode, nodeDepthLevel + 1, maxDepthLevel);
            relationshipsToCreate.add(Pair.of(node.getId(), child.getId()));
        }

        if(relationshipsToCreate.size() % 10000 == 0){
            tx.success();
            tx.finish();
            tx = graphDb.beginTx();
        }

        return node;
    }

    public Node createGraph(int childrenPerNode, int maxDepthLevel) {

        tx = graphDb.beginTx();
        logger.info("Creating nodes...");
        Node rootNode = null;
        try {
            rootNode = createNodeWithChildren(childrenPerNode, 0, maxDepthLevel);
            tx.success();
        } catch (Exception e) {
            tx.failure();
        } finally {
            tx.finish();
        }

        logger.info("Created nodes" + nodecnt);
        logger.info("Creating relationships..." + relationshipsToCreate.size());
        tx = graphDb.beginTx();
        int cnt = 0;
        try {
            for (Pair<Long, Long> nodes : relationshipsToCreate) {
                graphDb.getNodeById(nodes.first()).createRelationshipTo(graphDb.getNodeById(nodes.other()), CHILD);
                cnt++;
                if (cnt % 10000 == 0) {
                    tx.success();
                    tx.finish();
                    tx = graphDb.beginTx();
                    logger.info("Created relationships cnt: " + cnt);
                }
            }
            tx.success();
        } catch (Exception e) {
            tx.failure();
        } finally {
            tx.finish();
        }
        logger.info("Created relationships cnt: " + cnt);

        try (Transaction tx = graphDb.beginTx()) {
            return graphDb.getNodeById(rootNode.getId());
        }

    }

    public void depthFirstFindAll(Node startNode) {
        try (Transaction tx = graphDb.beginTx()) {
            TraversalDescription traversalDescription = Traversal.description()
                    .relationships(CHILD)
                    .depthFirst();
            Iterable<Node> nodes = traversalDescription.traverse(startNode).nodes();
            int cnt = 0;
            long startTime = System.currentTimeMillis();
            for (Node n : nodes) {
                cnt++;
            }
            //        logger.info("DEPTH FIRST find all at depth returned " + cnt + " nodes, took " + (System.currentTimeMillis() - startTime) + " millis.");
        }
    }

    public void breadthFirstFindAll(Node startNode) {
        try (Transaction tx = graphDb.beginTx()) {
            TraversalDescription traversalDescription = Traversal.description()
                    .relationships(CHILD)
                    .breadthFirst();
            Iterable<Node> nodes = traversalDescription.traverse(startNode).nodes();
            int cnt = 0;
            long startTime = System.currentTimeMillis();
            for (Node n : nodes) {
                cnt++;
            }
            //        logger.info("BREADTH FIRST find all at depth returned " + cnt + " nodes, took " + (System.currentTimeMillis() - startTime) + " millis.");
        }
    }


    static Set<Long> NODES_VISITED = new HashSet<Long>();


    public Pair<Integer, Long> depthFirstFindOne(Node startNode, final int depth, final int internalId) {

        try (Transaction tx = graphDb.beginTx()) {
            NODES_VISITED = new HashSet<Long>();;

            TraversalDescription traversalDescription = Traversal.description()
                    .relationships(CHILD)
                    .evaluator(new Evaluator() {
                        @Override
                        public Evaluation evaluate(Path path) {
                            if (((Integer) path.endNode().getProperty(LEVEL_ID)).equals(internalId) && ((Integer) path.endNode().getProperty("depthLevel")).equals(depth)) {
    //                        if (((Integer) path.endNode().getProperty(NODE_ID)).equals(internalId)) {
                                return Evaluation.INCLUDE_AND_PRUNE;
                            }
                            NODES_VISITED.add(path.endNode().getId());
                            return Evaluation.EXCLUDE_AND_CONTINUE;
                        }
                    })
                    .depthFirst().uniqueness(Uniqueness.NODE_GLOBAL);
            Iterable<Node> nodes = traversalDescription.traverse(startNode).nodes();
            long startTime = System.currentTimeMillis();
            if (!nodes.iterator().hasNext()) {
                throw new IllegalArgumentException("Not found:" + internalId + " on level:" + depth);
            }
            Node node = nodes.iterator().next();


            long timeElapsed = System.currentTimeMillis() - startTime;
            //        logger.info("DEPTH FIRST find by internal id " + internalId + " returned " + node.getId() + " node, took " + timeElapsed + " millis." + "nodes visited:" + NODES_VISITED);
            return Pair.of(NODES_VISITED.size(), timeElapsed);
        }
    }

    public Pair<Integer, Long> breadthFirstFindOne(Node startNode, final int depth, final int internalId) {
        try (Transaction tx = graphDb.beginTx()) {
            NODES_VISITED = new HashSet<Long>();
            TraversalDescription traversalDescription = Traversal.description()
                    .relationships(CHILD)
                    .evaluator(new Evaluator() {
                        @Override
                        public Evaluation evaluate(Path path) {
                            if (((Integer) path.endNode().getProperty(LEVEL_ID)).equals(internalId) && ((Integer) path.endNode().getProperty("depthLevel")).equals(depth)) {
    //                        if (((Integer) path.endNode().getProperty(NODE_ID)).equals(internalId)) {
                                return Evaluation.INCLUDE_AND_PRUNE;
                            }
                            NODES_VISITED.add(path.endNode().getId());
                            return Evaluation.EXCLUDE_AND_CONTINUE;
                        }
                    })
                    .breadthFirst();
            Iterable<Node> nodes = traversalDescription.traverse(startNode).nodes();
            long startTime = System.currentTimeMillis();
            if (!nodes.iterator().hasNext()) {
                throw new IllegalArgumentException("Not found:" + internalId + " on level:" + depth);
            }
            Node node = nodes.iterator().next();

            long timeElapsed = System.currentTimeMillis() - startTime;
            //        logger.info("BREADTH FIRST find by internal id " + internalId + " returned " + node.getId() + " node, took " + timeElapsed + " millis." + "nodes visited:" + NODES_VISITED);

            return Pair.of(NODES_VISITED.size(), timeElapsed);
        }
    }
}
