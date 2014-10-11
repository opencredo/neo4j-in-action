package com.manning.neo4jia.chapter09.advanced.indexbasedtrs.domain;

import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

/**
 * A Users viewing (and optional rating of) a Movie.
 */
@RelationshipEntity(type = "HAS_SEEN")
public class Viewing {

    Integer stars;

    @StartNode
    User user;
    @EndNode
    Movie movie;

    public Viewing() {
        super();
    }

    public Viewing(User user,Movie movie, Integer stars) {
        this.user = user;
        this.movie = movie;
        this.stars = stars;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public Integer getStars() {
        return stars;
    }

    public void setStars(Integer stars) {
        this.stars = stars;
    }

}
