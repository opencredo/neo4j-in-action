package com.manning.neo4jia.chapter09.advanced.domain;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedToVia;

/**
 * A Movie
 *
 * @author Nicki Watt
 */
@NodeEntity         // #1 This class is backed by a Neo4j Node
public class Movie {

    String title;

    @RelatedToVia(type = "HAS_SEEN" , direction = Direction.INCOMING)
    Iterable<Viewing> views;


    public Movie() {
        super();
    }

    public Movie(String title) {
        this.title = title;
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


}

