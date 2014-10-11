package com.manning.neo4jia.chapter08.expander;

import org.apache.commons.lang.RandomStringUtils;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.kernel.OrderedByTypeExpander;
import org.neo4j.kernel.StandardExpander;
import org.neo4j.kernel.Traversal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PathExpanderDemo {

    public static final DynamicRelationshipType WORKS_WITH = DynamicRelationshipType.withName("WORKS_WITH");
    public static final DynamicRelationshipType IS_FRIEND_OF = DynamicRelationshipType.withName("IS_FRIEND_OF");
    public static final DynamicRelationshipType LIKES = DynamicRelationshipType.withName("LIKES");
    private final GraphDatabaseService graphDb;

    public PathExpanderDemo(GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
    }

    public static void main(String[] args) {
        PathExpanderDemo demo = new PathExpanderDemo(new GraphDatabaseFactory().newEmbeddedDatabase("/tmp/neo4j/"+ RandomStringUtils.randomAlphanumeric(5)));
        Node john = demo.createData();


        demo.traverseUsingStandardExpander(john);
        demo.traverseUsingStandardExpanderBuilder(john);
        demo.traverseUsingOrderedByTypeExpander(john);
        demo.traverseUsingCustomExpander(john);

    }


    public void traverseUsingStandardExpander(Node john) {
        try (Transaction tx = graphDb.beginTx()) {
            System.out.println("---------------STANDARD EXPANDER:");
            TraversalDescription traversal = Traversal.description()
                    .expand((PathExpander) StandardExpander.DEFAULT
                            .add(WORKS_WITH)
                            .add(IS_FRIEND_OF)
                            .add(LIKES))
                    .evaluator(Evaluators.atDepth(2))
                    .evaluator(new Evaluator() {
                        @Override
                        public Evaluation evaluate(Path path) {
                            if (path.endNode().hasProperty("title")) {
                                return Evaluation.INCLUDE_AND_CONTINUE;
                            }
                            return Evaluation.EXCLUDE_AND_CONTINUE;
                        }
                    });
            Iterable<Node> nodes = traversal.traverse(john).nodes();
            for (Node n : nodes) {
                System.out.println(n.getProperty("title"));
            }
        }
    }

    public void traverseUsingStandardExpanderBuilder(Node john) {
        try (Transaction tx = graphDb.beginTx()) {
            System.out.println("---------------STANDARD EXPANDER:");
            TraversalDescription traversal = Traversal.description()
                    .relationships(WORKS_WITH)
                    .relationships(IS_FRIEND_OF)
                    .relationships(LIKES)
                    .evaluator(Evaluators.atDepth(2))
                    .evaluator(new Evaluator() {
                        @Override
                        public Evaluation evaluate(Path path) {
                            if (path.endNode().hasProperty("title")) {
                                return Evaluation.INCLUDE_AND_CONTINUE;
                            }
                            return Evaluation.EXCLUDE_AND_CONTINUE;
                        }
                    });
            Iterable<Node> nodes = traversal.traverse(john).nodes();
            for (Node n : nodes) {
                System.out.println(n.getProperty("title"));
            }
        }
    }

    public void traverseUsingOrderedByTypeExpander(Node john) {
        try (Transaction tx = graphDb.beginTx()) {
            System.out.println("---------------ORDERED BY TYPE EXPANDER:");

            PathExpander expander = new OrderedByTypeExpander().add(IS_FRIEND_OF).add(WORKS_WITH).add(LIKES);
            TraversalDescription traversal = Traversal.description().expand(expander).evaluator(Evaluators.atDepth(2)).evaluator(new Evaluator() {
                @Override
                public Evaluation evaluate(Path path) {
                    if (path.endNode().hasProperty("title")) {
                        return Evaluation.INCLUDE_AND_CONTINUE;
                    }
                    return Evaluation.EXCLUDE_AND_CONTINUE;
                }
            });
            Iterable<Node> nodes = traversal.traverse(john).nodes();
            for (Node n : nodes) {
                System.out.println(n.getProperty("title", "!!!USER" + n.getProperty("name", "THIS IS AN ERROR!")));
            }
        }
    }

    public void traverseUsingCustomExpander(Node john) {
        try (Transaction tx = graphDb.beginTx()) {
            System.out.println("---------------CUSTOM EXPANDER");
            HashMap<Integer, List<RelationshipType>> mappings = new HashMap<Integer, List<RelationshipType>>();
            mappings.put(0, Arrays.asList(new RelationshipType[]{WORKS_WITH, IS_FRIEND_OF}));
            mappings.put(1, Arrays.asList(new RelationshipType[]{LIKES}));

            TraversalDescription traversal = Traversal.description().expand(new DepthAwareExpander(mappings)).evaluator(Evaluators.atDepth(2));
            Iterable<Node> nodes = traversal.traverse(john).nodes();
            for (Node n : nodes) {
                System.out.println(n.getProperty("title"));
            }
        }
    }

    public Node createData() {
        Transaction tx = graphDb.beginTx();
        Node john = createUser(1, "John");
        Node kate = createUser(2, "Kate");
        Node emma = createUser(3, "Emma");
        Node jack = createUser(4, "Jack");
        Node alex = createUser(5, "Alex");
        john.createRelationshipTo(kate, WORKS_WITH);
        john.createRelationshipTo(emma, IS_FRIEND_OF);
        kate.createRelationshipTo(jack, WORKS_WITH);
        kate.createRelationshipTo(alex, WORKS_WITH);
        emma.createRelationshipTo(jack, WORKS_WITH);

        Node topGun = createMovie(1, "Top Gun");
        Node fargo = createMovie(2, "Fargo");
        Node alien = createMovie(3, "Alien");
        Node godfather = createMovie(4, "Godfather II");
        Node greatDictator = createMovie(5, "Great Dictator");

        john.createRelationshipTo(topGun, LIKES);
        kate.createRelationshipTo(fargo, LIKES);
        emma.createRelationshipTo(alien, LIKES);
        alex.createRelationshipTo(godfather, LIKES);
        jack.createRelationshipTo(godfather, LIKES);
        jack.createRelationshipTo(greatDictator, LIKES);

        tx.success();
        tx.finish();

        try (Transaction tx2 = graphDb.beginTx()) {
            return graphDb.getNodeById(1);
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

    private Node createMovie(int id, String title) {
        Node movie = graphDb.createNode();
        movie.setProperty("id", id);
        movie.setProperty("title", title);
        graphDb.index().forNodes("movies").add(movie, "id", id);
        graphDb.index().forNodes("movies").add(movie, "title", title);
        return movie;
    }
}
