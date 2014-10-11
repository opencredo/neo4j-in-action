package com.manning.neo4jia.chapter09.advanced.repository;

import com.manning.neo4jia.chapter09.advanced.domain.User;
import org.springframework.data.neo4j.repository.GraphRepository;

/**
 * SDN User Repository definition
 */
public interface UserRepository extends GraphRepository<User> {


}
