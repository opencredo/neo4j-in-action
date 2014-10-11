package com.manning.neo4jia.chapter01;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.tooling.GlobalGraphOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StopWatch;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author aleksavukotic
 */
public class Main {
    private static Logger logger = LoggerFactory.getLogger(Main.class);
    public static final int COUNT = 1000000;
    public static final int FRIENDS_PER_USER = 50;
    public static final int CACHE_WARMING_HITS = 10;
    public static final String NEO4J_STORE_DIR = "/tmp/neo4jia_chapter01";

    public static void main(String[] args){
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:META-INF/spring/module-context.xml");

        generateGraphData();

        runTraversalQuery(context);

//        generatedJdbcData(context);
//
//        runJdbcQuery(context);

    }

    private static interface Executor{
        void execute();
    }
    private static void measureExecution(Executor executor){
        long maxExecution = -1;
        long minExecution = -1;
        long totalExecutionTime = 0;
        for(int i=0; i<CACHE_WARMING_HITS; i++){
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            executor.execute();
            stopWatch.stop();
            long executionTime = stopWatch.getTotalTimeMillis();
            if(minExecution < 0 || executionTime < minExecution){
                minExecution = executionTime;
            }
            if(maxExecution < 0 || executionTime > maxExecution){
                maxExecution = executionTime;
            }
            totalExecutionTime+=executionTime;
        }

        double avg = totalExecutionTime/CACHE_WARMING_HITS;
        double withoutTop = (totalExecutionTime-maxExecution)/(CACHE_WARMING_HITS-1);
        logger.info("Max time: {} millis, min time: {} millis.", maxExecution, minExecution );
        logger.info("Average: {} millis, 90-percentile: {} millis.", avg, withoutTop );


    }
    private static void runJdbcQuery(ApplicationContext context) {
        final FriendsOfFriendsFinder jdbcFriendsOfFriendsFinder = (FriendsOfFriendsFinder)context.getBean("jdbcFriendsOfFriendsFinder");

        measureExecution(new Executor() {
            public void execute() {
                jdbcFriendsOfFriendsFinder.countFriendsOfFriends(2L);
            }
        });

        measureExecution(new Executor() {
            public void execute() {
                jdbcFriendsOfFriendsFinder.countFriendsOfFriendsDepth3(2L);
            }
        });

        measureExecution(new Executor() {
            public void execute() {
                jdbcFriendsOfFriendsFinder.countFriendsOfFriendsDepth4(2L);
            }
        });

        measureExecution(new Executor() {
            public void execute() {
                jdbcFriendsOfFriendsFinder.countFriendsOfFriendsDepth5(2L);
            }
        });

    }

    private static void runTraversalQuery(ApplicationContext context) {
        GraphDatabaseService graphdb = new GraphDatabaseFactory().newEmbeddedDatabase(NEO4J_STORE_DIR);

        final FriendsOfFriendsFinder neo4jFriendsOfFriendsFinder = new Neo4jFriendsOfFriendsFinder(graphdb);

        measureExecution(new Executor() {
            public void execute() {
                neo4jFriendsOfFriendsFinder.countFriendsOfFriends(2L);
            }
        });

        measureExecution(new Executor() {
            public void execute() {
                neo4jFriendsOfFriendsFinder.countFriendsOfFriendsDepth3(2L);
            }
        });

        measureExecution(new Executor() {
            public void execute() {
                neo4jFriendsOfFriendsFinder.countFriendsOfFriendsDepth4(2L);
            }
        });

        measureExecution(new Executor() {
            public void execute() {
                neo4jFriendsOfFriendsFinder.countFriendsOfFriendsDepth5(2L);
            }
        });

//        measureExecution(new Executor() {
//            public void execute() {
//                neo4jFriendsOfFriendsFinder.areConnectedViaFriendsUpToLevel4(6L, 2L);
//            }
//        });

        graphdb.shutdown();
    }

    private static void generateGraphData() {
        GraphDatabaseService graphdb = new GraphDatabaseFactory().newEmbeddedDatabase(NEO4J_STORE_DIR);
        int elementCnt = 0;

        try (Transaction tx = graphdb.beginTx()) {
            for(Node n : GlobalGraphOperations.at(graphdb).getAllNodes()){
                elementCnt++;
                for(Relationship r : n.getRelationships(Direction.OUTGOING, Constants.IS_FRIEND_OF)){
                    elementCnt++;
                }
            }
        }
        if(elementCnt != (COUNT + COUNT*FRIENDS_PER_USER)){
            logger.info("Not enough users/friends ({}, required {}), deleting existing neo4j database manually.", elementCnt, (COUNT + COUNT*FRIENDS_PER_USER));
            graphdb.shutdown();
            FileSystemUtils.deleteRecursively(new File(NEO4J_STORE_DIR));
            graphdb = new GraphDatabaseFactory().newEmbeddedDatabase(NEO4J_STORE_DIR);
            DataGenerator neo4jDataGenerator = new Neo4jDataGenerator(graphdb);
            neo4jDataGenerator.generateUsers(COUNT, FRIENDS_PER_USER);
        }
        graphdb.shutdown();
    }

    private static void generatedJdbcData(ApplicationContext context) {
        JdbcTemplate jdbcTemplate = context.getBean(JdbcTemplate.class);
//        jdbcTemplate.execute("create table if not exists T_USER ( id int not null, name varchar not null )");
//        jdbcTemplate.execute("create table if not exists T_USER_FRIEND ( id int not null, user_1 int not null, user_2 int not null )");

        Integer userCount = jdbcTemplate.query("select count(id) as cnt from t_user", new ResultSetExtractor<Integer>() {
            public Integer extractData(ResultSet rs) throws SQLException, DataAccessException {
                rs.next();
                return rs.getInt("cnt");
            }
        });
        Integer friendshipCount = jdbcTemplate.query("select count(id) as cnt from t_user_friend", new ResultSetExtractor<Integer>() {
            public Integer extractData(ResultSet rs) throws SQLException, DataAccessException {
                rs.next();
                return rs.getInt("cnt");
            }
        });
        if(userCount != COUNT || COUNT * FRIENDS_PER_USER != friendshipCount){
            logger.info("Not enough users/friends, recreating relational database");
            logger.info("Found {} users, requires {}", userCount, COUNT);
            logger.info("Found {} friendships, requires {}", friendshipCount, COUNT * FRIENDS_PER_USER);

            jdbcTemplate.execute("delete from t_user_friend");
            jdbcTemplate.execute("delete from t_user");

            DataGenerator jdbcDataGenerator = (DataGenerator)context.getBean("jdbcDataGenerator");
            jdbcDataGenerator.generateUsers(COUNT, FRIENDS_PER_USER);
        }else{
            logger.info("Database already populated ({} users), continuing.", userCount);
        }

    }
}
