#!/bin/bash -e

if [ -z "$NEO4J_HOME" ]; then
    echo "NEO4J_HOME needs to be set, please ensure neo4j-community-2.0.1 is installed and path set"
    exit 1
fi

$NEO4J_HOME/bin/neo4j stop
mvn clean test -f ../../../../../../chapter10/performance-demos-server/pom.xml    -P script-initiated-db-seeding

