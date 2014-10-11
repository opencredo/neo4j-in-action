#!/bin/bash -e

# Running each test from this script ensures that the JVM is started up fresh in each case, and that there
# is no leftover data loaded into the JVM memory from previous tests etc
#
# To only get the stopwatch details, recommended way to call script is via
# ./javarestbinding_adams_friends.sh | grep StopWatch
#

if [ -z "$NEO4J_HOME" ]; then
    echo "NEO4J_HOME needs to be set, please ensure neo4j-community-2.0.1 is installed and path set"
    exit 1
fi

mvn clean -f ../../../../../../chapter10/performance-demos-server/pom.xml  -P script-initiated-performance-demos

RUN_OTHER_EXAMPLES=false
JAVA_OPTS="-Xms178m -Xmx178m -XX:PermSize=128m -XX:MaxPermSize=128m"
MAVEN_OPTS=$JAVA_OPTS

# ------------------------------------------------------------------------------------------------
#   A) Run scripts used to generate performance numbers used in book
# ------------------------------------------------------------------------------------------------

./restart_neo4j_server.sh
echo '-------- TEST : StopWatch -> Result for Table 10.4 (Row 1 [Raw API]: Java REST binding) - COLD'
echo '-------- TEST : StopWatch -> TimedServerCallsTable104Demos#timeFindJFriendsUsingHypermediaDrivenRESTAPIStreamingOff [FROM COLD SERVER STARTUP] ---------'
./restart_neo4j_server.sh
mvn -Dtest=TimedServerCallsTable104Demos#timeFindJFriendsUsingHypermediaDrivenRESTAPIStreamingOff test -f ../../../../../../chapter10/performance-demos-server/pom.xml    -P script-initiated-performance-demos
echo '-------- TEST : StopWatch -> Result for Table 10.4 (Row 1 [Raw API]: Java REST binding) - WARM'
echo '-------- TEST : StopWatch -> TimedServerCallsTable104Demos#timeFindJFriendsUsingHypermediaDrivenRESTAPIStreamingOff [FROM WARM SERVER STARTUP] ---------'
mvn -Dtest=TimedServerCallsTable104Demos#timeFindJFriendsUsingHypermediaDrivenRESTAPIStreamingOff test -f ../../../../../../chapter10/performance-demos-server/pom.xml    -P script-initiated-performance-demos


# ------------------------------------------------------------------------------------------------
#   B) If desired, run additional scripts used to run other performance based tests
# ------------------------------------------------------------------------------------------------
if [ $RUN_OTHER_EXAMPLES = "true" ]; then

        echo '-------- TEST : StopWatch -> TimedServerCallsOtherDemos#timeFindJFriendsUsingHypermediaDrivenRESTAPIStreamingOn [FROM COLD SERVER STARTUP] ---------'
        ./restart_neo4j_server.sh
        mvn -Dtest=TimedServerCallsOtherDemos#timeFindJFriendsUsingHypermediaDrivenRESTAPIStreamingOn  test -f ../../../../../../chapter10/performance-demos-server/pom.xml   -P script-initiated-performance-demos
        echo '-------- TEST : StopWatch -> TimedServerCallsOtherDemos#timeFindJFriendsUsingHypermediaDrivenRESTAPIStreamingOn [FROM WARM SERVER STARTUP] ---------'
        mvn -Dtest=TimedServerCallsOtherDemos#timeFindJFriendsUsingHypermediaDrivenRESTAPIStreamingOn  test -f ../../../../../../chapter10/performance-demos-server/pom.xml   -P script-initiated-performance-demos

fi
