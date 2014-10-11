package com.manning.neo4jia.chapter09.simple.repository;

import com.manning.neo4jia.chapter09.simple.domain.Movie;
import org.springframework.data.neo4j.repository.GraphRepository;

/**
 * SDN Movie Repository definition
 */
public interface MovieRepository extends GraphRepository<Movie> {

}
