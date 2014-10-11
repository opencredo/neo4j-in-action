package com.manning.neo4jia.chapter04;

import com.google.common.collect.Iterables;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.*;
import org.neo4j.kernel.Traversal;
import org.neo4j.kernel.Uniqueness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author aleksavukotic
 */
public class Traversals {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public static final Long JOHN_JOHNSON_NODE_ID = 1L;
    private final GraphDatabaseService graphDb;

    public Traversals(GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
    }

    public Iterable<Node> findMoviesForUser_iterateThroughAllRelationships(){
        Node userJohn = graphDb.getNodeById(JOHN_JOHNSON_NODE_ID);

        Iterable<Relationship> allRelationships = userJohn.getRelationships();
        Set<Node> moviesForUserJohn = new HashSet<Node>();
        for (Relationship r : allRelationships) {
            if (r.getType().name().equalsIgnoreCase("HAS_SEEN")) {
                Node movieNode = r.getEndNode();
                moviesForUserJohn.add(movieNode);
            }
        }

        for (Node movie : moviesForUserJohn) {
            logger.info("Found movie: " + movie.getProperty("name"));
        }

        return moviesForUserJohn;
    }

    public Set<Node> findMoviesForUser_findRelationshipsByName() {
        Node userJohn = graphDb.getNodeById(JOHN_JOHNSON_NODE_ID);

        Iterable<Relationship> allRelationships = userJohn.getRelationships(MyRelationships.HAS_SEEN);
        Set<Node> moviesForUserJohn = new HashSet<Node>();
        for (Relationship r : allRelationships) {
            Node movieNode = r.getEndNode();
            moviesForUserJohn.add(movieNode);
        }

        for (Node movie : moviesForUserJohn) {
            logger.info("Found movie: " + movie.getProperty("name"));
        }

        return moviesForUserJohn;

    }

    public Iterable<Node> findMoviesThatFriendsLike_memoryIneffient() {
        Node userJohn = graphDb.getNodeById(JOHN_JOHNSON_NODE_ID);


        Set<Node> friends = new HashSet<Node>();
        for (Relationship r1 : userJohn.getRelationships(MyRelationships.IS_FRIEND_OF)) {
            Node friend = r1.getOtherNode(userJohn);
            friends.add(friend);
        }
        Set<Node> moviesFriendsLike = new HashSet<Node>();
        for (Node friend : friends) {
            for (Relationship r : friend.getRelationships(Direction.OUTGOING, MyRelationships.HAS_SEEN)) {
                moviesFriendsLike.add(r.getEndNode());
            }
        }
        Set<Node> moviesJohnLike = new HashSet<Node>();
        for (Relationship r : userJohn.getRelationships(Direction.OUTGOING, MyRelationships.HAS_SEEN)) {
            moviesJohnLike.add(r.getEndNode());
        }

        moviesFriendsLike.removeAll(moviesJohnLike);

        for (Node movie : moviesFriendsLike) {
            logger.info("Found movie: " + movie.getProperty("name"));
        }

        return moviesFriendsLike;

    }

    public Iterable<Node> findMoviesThatFriendsLike_memoryEfficient() {
        Node userJohn = graphDb.getNodeById(JOHN_JOHNSON_NODE_ID);


        Set<Node> moviesFriendsLike = new HashSet<Node>();
        for (Relationship r1 : userJohn.getRelationships(MyRelationships.IS_FRIEND_OF)) {
            Node friend = r1.getOtherNode(userJohn);
            for (Relationship r2 : friend.getRelationships(Direction.OUTGOING, MyRelationships.HAS_SEEN)) {
                Node movie = r2.getEndNode();
                boolean johnLikesIt = false;
                for (Relationship r3 : movie.getRelationships(Direction.INCOMING, MyRelationships.HAS_SEEN)) {
                    Node startNode = r3.getStartNode();
                    if (startNode.getId() == userJohn.getId()) {
                        johnLikesIt = true;
                    }
                }
                if (!johnLikesIt) {
                        moviesFriendsLike.add(movie);
                }
            }
        }

        for (Node movie : moviesFriendsLike) {
            logger.info("Found movie: " + movie.getProperty("name"));
        }

        return moviesFriendsLike;

    }

    public Iterable<Node> findMoviesThatMyFriendsLike_traversalFramework() {
        Node userJohn = graphDb.getNodeById(JOHN_JOHNSON_NODE_ID);


        TraversalDescription traversalMoviesFriendsLike =
                Traversal.description()
                        .relationships(MyRelationships.IS_FRIEND_OF)
                        .relationships(MyRelationships.HAS_SEEN, Direction.OUTGOING)
                        .depthFirst()
                        .uniqueness(Uniqueness.NODE_GLOBAL)
                        .evaluator(Evaluators.atDepth(2));
        org.neo4j.graphdb.traversal.Traverser traverser = traversalMoviesFriendsLike.traverse(userJohn);
        Iterable<Node> moviesFriendsLike = traverser.nodes();

        TraversalDescription traversalMoviesJohnLikes = Traversal.description().relationships(MyRelationships.HAS_SEEN, Direction.OUTGOING).evaluator(Evaluators.atDepth(1));
        Iterable<Node> moviesJohnLikes = traversalMoviesJohnLikes.traverse(userJohn).nodes();

        List<Node> moviesFriendsLikeList = Arrays.asList(Iterables.toArray(moviesFriendsLike, Node.class));
        List<Node> moviesJohnLikesList = Arrays.asList(Iterables.toArray(moviesJohnLikes, Node.class));

        moviesFriendsLikeList.removeAll(moviesJohnLikesList);

        for (Node movie : moviesFriendsLikeList) {
            logger.info("Found movie: " + movie.getProperty("name"));
        }

        return moviesFriendsLikeList;

    }

    public Iterable<Node> findMoviesThatMyFriendsLike_traversalFramework_customEvaluator_inline() {
        final Node userJohn = graphDb.getNodeById(JOHN_JOHNSON_NODE_ID);


        Iterable<Node> moviesFriendsLike = new HashSet<Node>();
        final TraversalDescription traversalMoviesJohnLikes = Traversal.description().relationships(MyRelationships.HAS_SEEN, Direction.OUTGOING).evaluator(Evaluators.atDepth(1));

        TraversalDescription traversalMoviesFriendsLike =
                Traversal.description()
                        .relationships(MyRelationships.IS_FRIEND_OF)
                        .relationships(MyRelationships.HAS_SEEN, Direction.OUTGOING)
                        .depthFirst()
                        .uniqueness(Uniqueness.NODE_GLOBAL)
                        .evaluator(Evaluators.atDepth(2))
                        .evaluator(new Evaluator() {
                            public Evaluation evaluate(Path path) {
                                Node endNode = path.endNode();
                                if (!endNode.hasProperty("type") || endNode.getProperty("type").equals("Movie")) {
                                    return Evaluation.EXCLUDE_AND_PRUNE;
                                }
                                for (Relationship r : endNode.getRelationships(Direction.INCOMING, MyRelationships.HAS_SEEN)) {
                                    if (r.getStartNode().equals(userJohn)) {
                                        return Evaluation.EXCLUDE_AND_CONTINUE;
                                    }
                                }
                                return Evaluation.INCLUDE_AND_CONTINUE;
                            }
                        });

        moviesFriendsLike = traversalMoviesFriendsLike.traverse(userJohn).nodes();

        for (Node movie : moviesFriendsLike) {
            logger.info("Found movie: " + movie.getProperty("name"));
        }

        return  moviesFriendsLike;
    }

    public Iterable<Node> findMoviesThatMyFriendsLike_traversalFramework_customEvaluator() {
        Node userJohn = graphDb.getNodeById(JOHN_JOHNSON_NODE_ID);


        Iterable<Node> moviesFriendsLike = new HashSet<Node>();
//        final TraversalDescription traversalMoviesJohnLikes = Traversal.description().relationships(hasSeenRelationshipType, Direction.OUTGOING).evaluator(Evaluators.atDepth(1));

        TraversalDescription traversalMoviesFriendsLike =
                Traversal.description()
                        .relationships(MyRelationships.IS_FRIEND_OF)
                        .relationships(MyRelationships.HAS_SEEN, Direction.OUTGOING)
                        .depthFirst()
                        .uniqueness(Uniqueness.NODE_GLOBAL)
                        .evaluator(Evaluators.atDepth(2))
                        .evaluator(new CustomNodeFilteringEvaluator(userJohn));

        moviesFriendsLike = traversalMoviesFriendsLike.traverse(userJohn).nodes();
        for (Node movie : moviesFriendsLike) {
            logger.info("Found movie: " + movie.getProperty("name"));
        }

        return moviesFriendsLike;
    }


}
