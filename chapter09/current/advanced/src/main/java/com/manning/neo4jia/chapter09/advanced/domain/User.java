package com.manning.neo4jia.chapter09.advanced.domain;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.*;
import org.springframework.data.neo4j.support.index.IndexType;

import java.util.HashSet;
import java.util.Set;

/**
 * A User
 */
@NodeEntity          // #1 This class is backed by a Neo4j Node
public class User {

    public String name;
    @Indexed(unique=true)
    String userId;

    // #4 Relationships to other node entities involving this node
    User referredBy;
    @RelatedTo(type = "IS_FRIEND_OF", direction = Direction.BOTH)
    Set<User> friends;
    @RelatedToVia(type = "HAS_SEEN")
    Set<Viewing> views;



    public User() {
        super();
    }

    public User(String userId, String name) {
        this.userId = userId;
        this.name = name;
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

    public void setFriends(Set<User> friends) {
        this.friends = friends;
    }

    public void addFriends(User friend) {
        if (friends == null)
            friends = new HashSet<User>();

        friends.add(friend);
        //friend.recepricolAddFriends(this);
    }

    private void recepricolAddFriends(User friend) {
        if (friends == null)
            friends = new HashSet<User>();

        friends.add(friend);
    }

    public void addViewing(Movie movie, Integer stars) {
        if (views == null)
            views = new HashSet<Viewing>();

        views.add(new Viewing(this,movie,stars));
    }


    public Set<Viewing> getViews() {
        return views;
    }

    public void setViews(Set<Viewing> views) {
        this.views = views;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public User getReferredBy() {
        return referredBy;
    }

    public void setReferredBy(User referredBy) {
        this.referredBy = referredBy;
    }

}
