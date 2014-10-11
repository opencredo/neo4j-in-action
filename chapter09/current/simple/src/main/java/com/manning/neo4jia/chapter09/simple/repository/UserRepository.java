package com.manning.neo4jia.chapter09.simple.repository;

import com.manning.neo4jia.chapter09.simple.domain.User;
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
    // Listing 9.13
    @Query( value = "match (n)-[r:IS_FRIEND_OF]-(friend)-[r2:IS_FRIEND_OF]-(fof) " +
                    "where id(n) = {0} " +
                    "return distinct fof")
    Iterable<User> getFriendsOfFriends(Long nodeId);

    /**
     * This version of the query method finds a Users friend of friends
     * based on a given userId.
     */
    @Query( value = "match (n:User {userId: {0} })-[r:IS_FRIEND_OF]-(friend)-[r2:IS_FRIEND_OF]-(fof) " +
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
            "MATCH (`user`:`User`) " +
            "WHERE `user`.`name` = {0} " +
            "RETURN `user`")
    Iterable<User> simulateFindByNameWhenUsingLabelBasedStrategy(String name);

    // --------------------------------------------------- \\

    Iterable<User> findByReferredByName(String name);

    @Query( value =
            "MATCH (`user`)-[:`referredBy`]->(`user_referredBy`) " +
            "WHERE `user_referredBy`.`name` = {0} " +
            "RETURN `user`")
    Iterable<User> simulateFindByReferredByNameWhenUsingLabelBasedStrategy(String name);


    // --------------------------------------------------- \\

    Iterable<User> findByNameLikeAndFriendsName(String name, String friendsName);

    @Query( value = "MATCH (`user`)-[:`IS_FRIEND_OF`]-(`user_friends`) " +
                    "WHERE `user`.`name` =~ {0} AND `user_friends`.`name` = {1} " +
                    "RETURN `user`")
    Iterable<User> simulateFindByNameLikeAndFriendsNameWhenUsingLabelBasedStrategy(String name, String friendsName);


    // --------------------------------------------------- \\

    Iterable<User> findByReferredByNameLike(String name);

    @Query( value =
            "MATCH (`user`)-[:`referredBy`]->(`user_referredBy`) " +
            "WHERE `user_referredBy`.`name` =~ {0} " +
            "RETURN `user`")
    Iterable<User> simulateFindByReferredByNameLikeWhenUsingLabelBasedStrategy(String name);

    // --------------------------------------------------- \\

}
