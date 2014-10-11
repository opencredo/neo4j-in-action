package com.manning.neo4jia.chapter01;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author aleksavukotic
 */
public class JdbcDataGenerator implements DataGenerator {
    private static Logger logger = LoggerFactory.getLogger(JdbcDataGenerator.class);

    public static final int BATCH_SIZE = 50000;
    private final JdbcTemplate jdbcTemplate;
    private List<String> sqlStatements = new ArrayList<String>(BATCH_SIZE);

    public JdbcDataGenerator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void generateUsers(int count, int friendsPerUser) {

        for (int i = 0; i < count; i++) {
            sqlStatements.add("insert into t_user (id, name) values (" + (i + 1) + ", '" + RandomStringUtils.randomAlphabetic(10) + "')");
            flush();
        }
        forceFlush();

        int id = 1;
        for (int i = 0; i < count; i++) {
            for (int j = 0; j < friendsPerUser; j++) {
                sqlStatements.add("insert into t_user_friend (id, user_1, user_2) values (" + id++ + ", " + (i + 1) + ", " + (getUniformPositiveInt(count-1)+1) + ")");
                flush();
            }
        }
        forceFlush();
    }

    private void flush() {
        if (sqlStatements.size() >= BATCH_SIZE) {
            forceFlush();
        }
    }

    private void forceFlush() {
        if(sqlStatements.isEmpty()){
            return;
        }
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        this.jdbcTemplate.batchUpdate(sqlStatements.toArray(new String[0]));
        stopWatch.stop();
        logger.info("Flushed {} elements in {} millis", sqlStatements.size(), stopWatch.getTotalTimeMillis());
        sqlStatements.clear();

    }

    public static int getUniformPositiveInt(int max) {
        Random random = new Random();

        return Math.abs(random.nextInt(max));
    }
}
