package com.manning.neo4jia.chapter08.uniqueness;

import org.apache.commons.lang.RandomStringUtils;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.kernel.Traversal;
import org.neo4j.kernel.Uniqueness;
import org.neo4j.tooling.GlobalGraphOperations;

import java.util.ArrayList;
import java.util.List;

public class UniquenessDemo {
    public static final DynamicRelationshipType KNOWS = DynamicRelationshipType.withName("KNOWS");
    public static final String USERS_INDEX_NAME = "users";
    public static final DynamicRelationshipType IS_FRIEND_OF = DynamicRelationshipType.withName("IS_FRIEND_OF");
    private final GraphDatabaseService graphDb;

    public UniquenessDemo(GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
    }

    public static void main(String[] args) {
        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase("/tmp/neo4j/"+ RandomStringUtils.randomAlphanumeric(5));
        UniquenessDemo demo = new UniquenessDemo(graphDb);
        demo.createData();


        Node jane = demo.findUserByName("Jane");
        Node ben = demo.findUserByName("Ben");

        try (Transaction tx = graphDb.beginTx()) {
            System.out.println("----------PATHS:NODE_PATH------------");
            List<Path> paths = demo.pathThatICanUseToIntroduceMeTo(jane, ben, Uniqueness.NODE_PATH);
            for (Path p : paths) {
                System.out.println(p);
            }
            System.out.println("----------PATHS:NODE_GLOBAL------------");
            paths = demo.pathThatICanUseToIntroduceMeTo(jane, ben, Uniqueness.NODE_GLOBAL);
            for (Path p : paths) {
                System.out.println(p);
            }
            List<Node> nodes = demo.friendsThatCanIntroduceMeTo(jane, ben, Uniqueness.NODE_GLOBAL);
            System.out.println("----------NODES:NODE_GLOBAL------------");
            for (Node n : nodes) {
                System.out.println(n.getProperty("name"));
            }
            System.out.println("----------NODES:NODE_PATH------------");
            nodes = demo.friendsThatCanIntroduceMeTo(jane, ben, Uniqueness.NODE_PATH);
            for (Node n : nodes) {
                System.out.println(n.getProperty("name"));
            }
        }
//        Node john = demo.findUserByName("John");
//        Node kate = demo.findUserByName("Kate");
//        demo.createRelationship(kate, john, IS_FRIEND_OF);
//
//
//        System.out.println("----------PATHS2:NODE_PATH------------");
//               paths = demo.pathThatICanUseToIntroduceMeTo(jane, ben, Uniqueness.NODE_PATH);
//               for (Path p : paths) {
//                   System.out.println(p);
//               }
//
//        System.out.println("----------PATHS2:RELATIONSHIP_PATH------------");
//                       paths = demo.pathThatICanUseToIntroduceMeTo(jane, ben, Uniqueness.RELATIONSHIP_PATH);
//                       for (Path p : paths) {
//                           System.out.println(p);
//                       }
    }

    public void createRelationship(Node start, Node end, DynamicRelationshipType relationshipType) {
        Transaction tx = graphDb.beginTx();
        start.createRelationshipTo(end, relationshipType);
        tx.success();
        tx.finish();
    }

    public Node findUserByName(String name) {
        try (Transaction tx = graphDb.beginTx()) {
            return graphDb.index().forNodes(USERS_INDEX_NAME).get("name", name).getSingle();
        }
    }

    public void createData() {
        Transaction tx = graphDb.beginTx();

        try {
            Node jane = createUser(1, "Jane");
            Node john = createUser(2, "John");
            Node kate = createUser(3, "Kate");
            Node jack = createUser(4, "Jack");
            Node ben = createUser(5, "Ben");
            Node emma = createUser(6, "Emma");

            jane.createRelationshipTo(john, KNOWS);
            jane.createRelationshipTo(kate, KNOWS);
            john.createRelationshipTo(jack, KNOWS);
            john.createRelationshipTo(ben, KNOWS);
            john.createRelationshipTo(kate, KNOWS);
            kate.createRelationshipTo(emma, KNOWS);


            tx.success();
        } catch (Exception e) {
            tx.failure();
        } finally {
            tx.finish();
        }

        try (Transaction tx2 = graphDb.beginTx()) {
            for (Node n : GlobalGraphOperations.at(graphDb).getAllNodes()) {
                System.out.println(n + ", name:" + n.getProperty("name") + ", id:" + n.getProperty("id"));
            }
        }
    }

    private Node createUser(int id, String name) {
        Node user = graphDb.createNode();
        user.setProperty("id", id);
        user.setProperty("name", name);
        graphDb.index().forNodes("users").add(user, "id", id);
        graphDb.index().forNodes("users").add(user, "name", name);
        return user;
    }

    public List<Node> friendsThatCanIntroduceMeTo(Node me, final Node target, Uniqueness uniqueness) {

        TraversalDescription description = Traversal.description()
                .relationships(KNOWS)
                .relationships(IS_FRIEND_OF)
                .evaluator(new Evaluator() {
                    @Override
                    public Evaluation evaluate(Path path) {
                        Node currentNode = path.endNode();
                        if (currentNode.getId() == target.getId()) {
                            return Evaluation.EXCLUDE_AND_PRUNE;
                        }
                        Path singlePath = GraphAlgoFactory.shortestPath(Traversal.expanderForTypes(KNOWS, Direction.BOTH, IS_FRIEND_OF, Direction.BOTH), 1).findSinglePath(currentNode, target);
                        if (singlePath != null) {
                            //direct link exists
                            return Evaluation.INCLUDE_AND_CONTINUE;
                        } else {
                            return Evaluation.EXCLUDE_AND_CONTINUE;
                        }
                    }
                })
//                .evaluator(Evaluators.atDepth(1))
                .uniqueness(uniqueness);
        Iterable<Node> nodes = description.traverse(me).nodes();
        ArrayList<Node> result = new ArrayList<Node>();
        IteratorUtil.addToCollection(nodes, result);
        return result;

    }

    public List<Path> pathThatICanUseToIntroduceMeTo(Node me, final Node target, Uniqueness uniqueness) {

        TraversalDescription description = Traversal.description()
                .relationships(KNOWS)
                .relationships(IS_FRIEND_OF)
                .evaluator(new Evaluator() {
                    @Override
                    public Evaluation evaluate(Path path) {
                        Node currentNode = path.endNode();
                        if (currentNode.getId() == target.getId()) {
                            return Evaluation.EXCLUDE_AND_PRUNE;
                        }
                        Path singlePath = GraphAlgoFactory.shortestPath(Traversal.expanderForTypes(KNOWS, Direction.BOTH, IS_FRIEND_OF, Direction.BOTH), 1).findSinglePath(currentNode, target);
                        if (singlePath != null) {
                            //direct link exists
                            return Evaluation.INCLUDE_AND_CONTINUE;
                        } else {
                            return Evaluation.EXCLUDE_AND_CONTINUE;
                        }
                    }
                })
                .uniqueness(uniqueness);
        ArrayList<Path> result = new ArrayList<Path>();
        try (Transaction tx = graphDb.beginTx()) {
            IteratorUtil.addToCollection(description.traverse(me).iterator(), result);
        }
        return result;

    }
}
