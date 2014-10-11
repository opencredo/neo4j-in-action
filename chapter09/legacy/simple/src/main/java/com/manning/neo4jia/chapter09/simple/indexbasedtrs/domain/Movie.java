package com.manning.neo4jia.chapter09.simple.indexbasedtrs.domain;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedToVia;

/**
 * A Movie
 */
@NodeEntity
public class Movie {

    @GraphId
    Long nodeId;
    String title;

    @RelatedToVia(direction = Direction.INCOMING)
    Iterable<Viewing> views;


    public Movie() {
        super();
    }

    public Movie(String title) {
        this.title = title;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Iterable<Viewing> getViews() {
        return views;
    }

    public void setViews(Iterable<Viewing> views) {
        this.views = views;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Movie movie = (Movie) o;

        if (nodeId != null ? !nodeId.equals(movie.nodeId) : movie.nodeId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return nodeId != null ? nodeId.hashCode() : 0;
    }
}

