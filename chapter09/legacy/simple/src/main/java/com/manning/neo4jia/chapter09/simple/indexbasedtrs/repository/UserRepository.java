package com.manning.neo4jia.chapter09.simple.indexbasedtrs.repository;

import com.manning.neo4jia.chapter09.simple.indexbasedtrs.domain.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;

/**
 * SDN User Repository definition
 */
public interface UserRepository extends GraphRepository<User> {

    /**
     * This version of the query method finds a Users friend of friends
     * based on a nodeId.
     */
    @Query( value =
            "match (n)-[r:IS_FRIEND_OF]-(friend)-[r2:IS_FRIEND_OF]-(fof) " +
            "where id(n) = {0} " +
            "return distinct fof")
    Iterable<User> getFriendsOfFriends(Long nodeId);

    /**
     * This version of the query method finds a Users friend of friends
     * based on a given userId.
     */
    @Query( value =
            "match (n:User {userId: {0} })-[r:IS_FRIEND_OF]-(friend)-[r2:IS_FRIEND_OF]-(fof) " +
            "return distinct fof")
    Iterable<User> getFriendsOfFriends(String userId);

    // --------------------------------------------------- \\

    User findDistinctByName(String name);
    User findDistinctUserByName(String name);

    // --------------------------------------------------- \\

    User findSingleByName(String name);
    Iterable<User> name(String name);
    Iterable<User> getByName(String name);
    Iterable<User> getUserByName(String name);
    Iterable<User> readByName(String name);
    Iterable<User> readUserByName(String name);
    Iterable<User> findByName(String name);
    Iterable<User> findUserByName(String name);
    Iterable<User> findByName(String name, Sort sort);

    @Query( value =
            "start n=node:__types__(className=\"User\")  " +
                    "where n.name = {0} " +
                    "return n")
    Iterable<User> simulateFindByNameWhenUsingIndexedBasedStrategy(String name);

    // --------------------------------------------------- \\

    Iterable<User> findByReferredByName(String name);

    @Query( value =
            "start n=node:__types__(className=\"User\")  " +
                    "match (n)-[:referredBy]->(x)" +
                    "where x.name = {0} " +
                    "return n")
    Iterable<User> simulateFindByReferredByNameWhenUsingIndexedBasedStrategy(String name);

    // --------------------------------------------------- \\

    Iterable<User> findByNameLikeAndFriendsName(String name, String friendsName);

    @Query( value = "start user=node:__types__(className=\"User\") " +
                    "match user-[:IS_FRIEND_OF]-user_friends " +
                    "where user.name =~ {0} and user_friends.name = {1} " +
                    "return user")
    Iterable<User> simulateFindByNameLikeAndFriendsNameWhenUsingIndexedBasedStrategy(String name, String friendsName);

    // --------------------------------------------------- \\

    Iterable<User> findByReferredByNameLike(String name);

    @Query( value =
            "start n=node:__types__(className=\"User\")  " +
                    "match (n)-[:referredBy]->(x)" +
                    "where x.name =~ {0} " +
                    "return n")
    Iterable<User> simulateFindByReferredByNameLikeWhenUsingIndexedBasedStrategy(String name);

    // --------------------------------------------------- \\

}
