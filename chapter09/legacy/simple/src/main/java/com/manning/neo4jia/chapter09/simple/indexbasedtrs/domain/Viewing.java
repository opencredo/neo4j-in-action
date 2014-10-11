package com.manning.neo4jia.chapter09.simple.indexbasedtrs.domain;

import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

/**
 * A Users viewing (and optional rating of) a Movie.
 */
@RelationshipEntity(type = "HAS_SEEN")     // #5 This class is backed by a Neo4j Relationship
public class Viewing {

    public static final Integer ONE_STAR    = 1;
    public static final Integer TWO_STARS   = 2;
    public static final Integer THREE_STARS = 3;
    public static final Integer FOUR_STARS  = 4;
    public static final Integer FIVE_STARS  = 5;

    // #2 Stored as node properties within graph
    Integer stars;

    // #3 Special annotation required when using simple object mapping
    @GraphId
    Long relationshipId;

    // #6 References to Node Entities on either side of relationship
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

    public Long getRelationshipId() {
        return relationshipId;
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

    @Override
    public int hashCode() {
        return relationshipId != null
                ? relationshipId.hashCode()
                : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Viewing))
            return false;
        Viewing that = (Viewing)obj;

        return (relationshipId != null && that.relationshipId != null &&
                relationshipId.equals(that.relationshipId));
    }


}
