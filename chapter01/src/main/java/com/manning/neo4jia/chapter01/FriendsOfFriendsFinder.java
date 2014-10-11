package com.manning.neo4jia.chapter01;

import java.util.List;

/**
 * @author aleksavukotic
 */
public interface FriendsOfFriendsFinder {

    Long countFriendsOfFriends(Long userId);

    Long countFriendsOfFriendsDepth3(Long userId);

    Long countFriendsOfFriendsDepth4(Long userId);

    Long countFriendsOfFriendsDepth5(Long userId);

    boolean areConnectedViaFriendsUpToLevel4(Long user1, Long user2);
}
