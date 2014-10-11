package com.manning.neo4jia.chapter01;

/**
 * @author aleksavukotic
 */
public interface DataGenerator {
    void generateUsers(int count, int friendsPerUser);
}
