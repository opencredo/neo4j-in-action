package com.manning.neo4jia.chapter09.simple.domain;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
    Various aspects of this class have been used in code listings, specifically
    - Listing 9.1 Initial POJO modeling attempt (Pre SDN annotations)
    - Listing 9.2 SDN annotated domain model
 */
@NodeEntity
public class User {

    @GraphId
    Long nodeId;

    @Indexed(unique=true)
    String userId;

    String name;

    @RelatedTo(type = "IS_FRIEND_OF", direction = Direction.BOTH)
    Set<User> friends = new HashSet<User>();
    @RelatedToVia
    Set<Viewing> views = new HashSet<Viewing>();

    User referredBy;
    PhoneNumber phoneNumber;
    PhoneNumberWithoutConverter phoneNumberWithoutConverter;

    @Query( value = "match (n)-[r]-(friend)-[r2]-(fof) " +
                    "where id(n) = {self} " +
                    "and   type(r)  =  {friendRelName}" +
                    "and   type(r2) =  {friendRelName}" +
                    "return distinct fof",
            params = {"friendRelName","IS_FRIEND_OF"})
    Iterable<User> friendsOfFriendsStyle2;

    // Ref 9.5.1 code snippets
    @Query( value =
            "match (n)-[r:IS_FRIEND_OF]-(friend)-[r2:IS_FRIEND_OF]-(fof) " +
            "where id(n) = {self} " +
            "return distinct fof")
    Iterable<User> friendsOfFriends;

    // Ref 9.5.1 code snippets
    @Query(value =
            "match (n)-[r]-(friend)-[r2:IS_FRIEND_OF]-(fof) " +
            "where id(n) = {self} " +
            "and type(r)  =  {friendRelName}" +
            "return  friend.name as friendName , count(fof) as numFriends",
            params = {"friendRelName","IS_FRIEND_OF"})
    Iterable<Map<String,Object>> friendsOfFriendsCount;

    // Ref 9.5.1 code snippets
    @Query(value = "match (n)-[r:IS_FRIEND_OF]-(friend)-[r2:IS_FRIEND_OF]-(fof) " +
                   "where id(n) = {self} " +
                   "return count(distinct fof)")
    Long totalNumFriendsOfFriends;

    public User() {
        super();
    }

    public User(String userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<User> getFriends() {
        return friends;
    }

    public void addFriend(User friend) {
        friends.add(friend);
    }

    public void addViewing(Movie movie, Integer stars) {
        views.add(new Viewing(this,movie,stars));
    }


    public Set<Viewing> getViews() {
        return views;
    }

    public String getUserId() {
        return userId;
    }

    public User getReferredBy() {
        return referredBy;
    }

    public void setReferredBy(User referredBy) {
        this.referredBy = referredBy;
    }

    public Iterable<User> getFriendsOfFriendsStyle2() {
        return friendsOfFriendsStyle2;
    }

    public Iterable<User> getFriendsOfFriends() {
        return friendsOfFriends;
    }

    public Iterable<Map<String, Object>> getFriendsOfFriendsCount() {
        return friendsOfFriendsCount;
    }

    public Long getTotalNumFriendsOfFriends() {
        return totalNumFriendsOfFriends;
    }

    public PhoneNumber getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(PhoneNumber phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public PhoneNumberWithoutConverter getPhoneNumberWithoutConverter() {
        return phoneNumberWithoutConverter;
    }

    public void setPhoneNumberWithoutConverter(PhoneNumberWithoutConverter phoneNumberWithoutConverter) {
        this.phoneNumberWithoutConverter = phoneNumberWithoutConverter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (nodeId != null ? !nodeId.equals(user.nodeId) : user.nodeId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return nodeId != null ? nodeId.hashCode() : 0;
    }
}
