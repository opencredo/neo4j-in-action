package com.manning.neo4jia.chapter01;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.util.StopWatch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author aleksavukotic
 */
public class JdbcFriendsOfFriendsFinder implements FriendsOfFriendsFinder{

    private static Logger logger = LoggerFactory.getLogger(JdbcFriendsOfFriendsFinder.class);

    private final JdbcTemplate jdbcTemplate;

    public JdbcFriendsOfFriendsFinder(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long countFriendsOfFriends(final Long userId) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final String sql = "select count(distinct f2.user_2) as cnt from t_user_friend f1 inner join t_user_friend f2 on f1.user_2 = f2.user_1 where f1.user_1 = ?";

        Long result = jdbcTemplate.query(new PreparedStatementCreator() {
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement preparedStatement = con.prepareStatement(sql);
                preparedStatement.setLong(1, userId);
                return preparedStatement;
            }
        },
        new ResultSetExtractor<Long>() {
            public Long extractData(ResultSet rs) throws SQLException, DataAccessException {
                rs.next();
                return rs.getLong("cnt");
            }
        });
        stopWatch.stop();
        logger.info("JDBC: Found {} friends of friends for user {}, took "+stopWatch.getTotalTimeMillis() + " millis.", result, userId);
        return result;
    }

    public Long countFriendsOfFriendsDepth3(final Long userId) {
                StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final String sql = "select count(distinct f3.user_2) as cnt from t_user_friend f1 inner join t_user_friend f2 on f1.user_2 = f2.user_1 inner join t_user_friend f3 on f2.user_2 = f3.user_1 where f1.user_1 = ?";

        Long result = jdbcTemplate.query(new PreparedStatementCreator() {
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement preparedStatement = con.prepareStatement(sql);
                preparedStatement.setLong(1, userId);
                return preparedStatement;
            }
        },
        new ResultSetExtractor<Long>() {
            public Long extractData(ResultSet rs) throws SQLException, DataAccessException {
                rs.next();
                return rs.getLong("cnt");
            }
        });
        stopWatch.stop();
        logger.info("JDBC: Found {} friends of friends depth 3 for user {}, took "+stopWatch.getTotalTimeMillis() + " millis.", result, userId);
        return result;
    }

    public Long countFriendsOfFriendsDepth4(final Long userId) {
                StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final String sql = "select count(distinct f4.user_2) as cnt from t_user_friend f1 inner join t_user_friend f2 on f1.user_2 = f2.user_1 inner join t_user_friend f3 on f2.user_2 = f3.user_1 inner join t_user_friend f4 on f3.user_2 = f4.user_1 where f1.user_1 = ?";

        Long result = jdbcTemplate.query(new PreparedStatementCreator() {
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement preparedStatement = con.prepareStatement(sql);
                preparedStatement.setLong(1, userId);
                return preparedStatement;
            }
        },
        new ResultSetExtractor<Long>() {
            public Long extractData(ResultSet rs) throws SQLException, DataAccessException {
                rs.next();
                return rs.getLong("cnt");
            }
        });
        stopWatch.stop();
        logger.info("JDBC: Found {} friends of friends depth 4 for user {}, took "+stopWatch.getTotalTimeMillis() + " millis.", result, userId);
        return result;
    }

    public Long countFriendsOfFriendsDepth5(final Long userId) {
                        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final String sql = "select count(distinct f5.user_2) as cnt from t_user_friend f1 inner join t_user_friend f2 on f1.user_2 = f2.user_1 inner join t_user_friend f3 on f2.user_2 = f3.user_1 inner join t_user_friend f4 on f3.user_2 = f4.user_1 inner join t_user_friend f5 on f5.user_2 = f5.user_1 where f1.user_1 = ?";

        Long result = jdbcTemplate.query(new PreparedStatementCreator() {
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement preparedStatement = con.prepareStatement(sql);
                preparedStatement.setLong(1, userId);
                return preparedStatement;
            }
        },
        new ResultSetExtractor<Long>() {
            public Long extractData(ResultSet rs) throws SQLException, DataAccessException {
                rs.next();
                return rs.getLong("cnt");
            }
        });
        stopWatch.stop();
        logger.info("JDBC: Found {} friends of friends depth 5 for user {}, took "+stopWatch.getTotalTimeMillis() + " millis.", result, userId);
        return result;
    }

    public boolean areConnectedViaFriendsUpToLevel4(Long user1, Long user2) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
