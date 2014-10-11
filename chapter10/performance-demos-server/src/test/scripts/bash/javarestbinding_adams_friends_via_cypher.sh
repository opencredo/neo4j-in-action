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

mvn clean -f ../../../../../../chapter10/performance-demos-server/pom.xml -P script-initiated-performance-demos

RUN_OTHER_EXAMPLES=false
JAVA_OPTS="-Xms512m -Xmx512m -XX:PermSize=256m -XX:MaxPermSize=256m"
MAVEN_OPTS=$JAVA_OPTS

# ------------------------------------------------------------------------------------------------
#   A) Run scripts used to generate performance numbers used in book
# ------------------------------------------------------------------------------------------------

echo '-------- TEST : StopWatch -> Result for Table 10.4 (Row 2 [Cypher]: Java REST binding) - COLD'
echo '-------- TEST : StopWatch -> TimedServerCallsTable104Demos#timeFindJFriendsInOneCypherCallStreamingOff [FROM COLD SERVER STARTUP]---------'
./restart_neo4j_server.sh
mvn -Dtest=TimedServerCallsTable104Demos#timeFindJFriendsInOneCypherCallStreamingOff test -f ../../../../../../chapter10/performance-demos-server/pom.xml  -P script-initiated-performance-demos
echo '-------- TEST : StopWatch -> Result for Table 10.4 (Row 2 [Cypher]: Java REST binding) - WARM'
echo '-------- TEST : StopWatch -> TimedServerCallsTable104Demos#timeFindJFriendsInOneCypherCallStreamingOff [FROM WARM SERVER STARTUP]---------'
mvn -Dtest=TimedServerCallsTable104Demos#timeFindJFriendsInOneCypherCallStreamingOff test -f ../../../../../../chapter10/performance-demos-server/pom.xml  -P script-initiated-performance-demos

# ------------------------------------------------------------------------------------------------
#   B) If desired, run additional scripts used to run other performance based tests
# ------------------------------------------------------------------------------------------------
if [ $RUN_OTHER_EXAMPLES = "true" ]; then

        echo '-------- TEST : StopWatch -> TimedServerCallsOtherDemos#timeFindJFriendsInOneCypherCallStreamingOn [FROM COLD SERVER STARTUP] ---------'
        ./restart_neo4j_server.sh
        mvn -Dtest=TimedServerCallsOtherDemos#timeFindJFriendsInOneCypherCallStreamingOn test -f ../../../../../../chapter10/performance-demos-server/pom.xml  -P script-initiated-performance-demos
        echo '-------- TEST : StopWatch -> TimedServerCallsOtherDemos#timeFindJFriendsInOneCypherCallStreamingOn [FROM WARM SERVER STARTUP] ---------'
        mvn -Dtest=TimedServerCallsOtherDemos#timeFindJFriendsInOneCypherCallStreamingOn test -f ../../../../../../chapter10/performance-demos-server/pom.xml   -P script-initiated-performance-demos


        echo '-------- TEST : StopWatch -> TimedServerCallsOtherDemos#timeFindAllFriendDetailsX1InOneCypherCallStreamingOff [FROM COLD SERVER STARTUP] ---------'
        ./restart_neo4j_server.sh
        mvn -Dtest=TimedServerCallsOtherDemos#timeFindAllFriendDetailsX1InOneCypherCallStreamingOff test -f ../../../../../../chapter10/performance-demos-server/pom.xml  -P script-initiated-performance-demos
        echo '-------- TEST : StopWatch -> TimedServerCallsOtherDemos#timeFindAllFriendDetailsX1InOneCypherCallStreamingOff [FROM WARM SERVER STARTUP] ---------'
        mvn -Dtest=TimedServerCallsOtherDemos#timeFindAllFriendDetailsX1InOneCypherCallStreamingOff test -f ../../../../../../chapter10/performance-demos-server/pom.xml   -P script-initiated-performance-demos



        echo '-------- TEST : StopWatch -> TimedServerCallsOtherDemos#timeFindAllFriendDetailsX1InOneCypherCallStreamingOn [FROM COLD SERVER STARTUP] ---------'
        ./restart_neo4j_server.sh
        mvn -Dtest=TimedServerCallsOtherDemos#timeFindAllFriendDetailsX1InOneCypherCallStreamingOn test -f ../../../../../../chapter10/performance-demos-server/pom.xml     -P script-initiated-performance-demos
        echo '-------- TEST : StopWatch -> TimedServerCallsOtherDemos#timeFindAllFriendDetailsX1InOneCypherCallStreamingOn [FROM WARM SERVER STARTUP] ---------'
        mvn -Dtest=TimedServerCallsOtherDemos#timeFindAllFriendDetailsX1InOneCypherCallStreamingOn test -f ../../../../../../chapter10/performance-demos-server/pom.xml     -P script-initiated-performance-demos


        # Note : Needed to set forkmode to never on chapter10 pom.xml in order for these args to be picked up.
        #        The other alternatives is to set them in the argLine configuration property
        export JAVA_OPTS=" -Xms1024m -Xmx1024m -XX:PermSize=256m -XX:MaxPermSize=256m "
        export MAVEN_OPTS=$JAVA_OPTS


        echo '-------- TEST : StopWatch -> TimedServerCallsOtherDemos#timeFindAllFriendDetailsXFactorIncreaseInOneCypherCallStreamingOn [FROM COLD SERVER STARTUP] ---------'
        ./restart_neo4j_server.sh
        mvn -Dtest=TimedServerCallsOtherDemos#timeFindAllFriendDetailsXFactorIncreaseInOneCypherCallStreamingOn test -f ../../../../../../chapter10/performance-demos-server/pom.xml   -P script-initiated-performance-demos
        echo '-------- TEST : StopWatch -> TimedServerCallsOtherDemos#timeFindAllFriendDetailsXFactorIncreaseInOneCypherCallStreamingOn [FROM WARM SERVER STARTUP] ---------'
        mvn -Dtest=TimedServerCallsOtherDemos#timeFindAllFriendDetailsXFactorIncreaseInOneCypherCallStreamingOn test -f ../../../../../../chapter10/performance-demos-server/pom.xml    -P script-initiated-performance-demos


        echo '-------- TEST : StopWatch -> TimedServerCallsOtherDemos#timeFindAllFriendDetailsXFactorIncreaseInOneCypherCallStreamingOff [FROM COLD SERVER STARTUP] ---------'
        ./restart_neo4j_server.sh
        mvn -Dtest=TimedServerCallsOtherDemos#timeFindAllFriendDetailsXFactorIncreaseInOneCypherCallStreamingOff test -f ../../../../../../chapter10/performance-demos-server/pom.xml   -P script-initiated-performance-demos
        echo '-------- TEST : StopWatch -> TimedServerCallsOtherDemos#timeFindAllFriendDetailsXFactorIncreaseInOneCypherCallStreamingOff [FROM WARM SERVER STARTUP] ---------'
        mvn -Dtest=TimedServerCallsOtherDemos#timeFindAllFriendDetailsXFactorIncreaseInOneCypherCallStreamingOff test -f ../../../../../../chapter10/performance-demos-server/pom.xml    -P script-initiated-performance-demos

fi



