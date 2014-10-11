#!/bin/bash -e

# Assumes the use of the built in bash time command NOT /usr/bin/time
# TIMEFORMAT=%R

SHOW_CONTENT='-s -S -o /dev/null'
#SHOW_CONTENT=' '
RUN_OTHER_EXAMPLES=false

# ------------------------------------------------------------------------------------------------
#   A) Run scripts used to generate performance numbers used in book
# ------------------------------------------------------------------------------------------------

echo '-------- REST API to get All of Adams friends whose names start with J (Streaming off) [FROM COLD SERVER STARTUP] ---------'
echo '--------  Result for Table 10.4 (Row 2 [Cypher]: curl client) - COLD'
./restart_neo4j_server.sh
time  curl -X POST $SHOW_CONTENT -H "Accept: application/json" -H "X-Stream: false" -H "Content-type: application/json" -d @../../resources/curl/adams_friends_startingwithj.cypher  -w "total time (seconds) = %{time_total}\ntotal download size (bytes)=%{size_download}\n\n"  http://localhost:7474/db/data/cypher
echo '-------- REST API to get All of Adams friends whose names start with J (Streaming off) [FROM WARM SERVER STARTUP] ---------'
echo '--------  Result for Table 10.4 (Row 2 [Cypher]: curl client) - WARM'
time  curl -X POST $SHOW_CONTENT -H "Accept: application/json" -H "X-Stream: false" -H "Content-type: application/json" -d @../../resources/curl/adams_friends_startingwithj.cypher  -w "total time (seconds) = %{time_total}\ntotal download size (bytes)=%{size_download}\n\n"  http://localhost:7474/db/data/cypher
echo '--------  Result for Table 10.4 (Row 2 [Cypher]: curl client) - WARM2'
time  curl -X POST $SHOW_CONTENT -H "Accept: application/json" -H "X-Stream: false" -H "Content-type: application/json" -d @../../resources/curl/adams_friends_startingwithj.cypher  -w "total time (seconds) = %{time_total}\ntotal download size (bytes)=%{size_download}\n\n"  http://localhost:7474/db/data/cypher
echo '--------  Result for Table 10.4 (Row 2 [Cypher]: curl client) - WARM3'
time  curl -X POST $SHOW_CONTENT -H "Accept: application/json" -H "X-Stream: false" -H "Content-type: application/json" -d @../../resources/curl/adams_friends_startingwithj.cypher  -w "total time (seconds) = %{time_total}\ntotal download size (bytes)=%{size_download}\n\n"  http://localhost:7474/db/data/cypher

# ------------------------------------------------------------------------------------------------
#   B) If desired, run additional scripts used to run other performance based tests
# ------------------------------------------------------------------------------------------------
if [ $RUN_OTHER_EXAMPLES = "true" ]; then

        echo '-------- REST API to get All of Adams friends whose names start with J (Streaming on) [FROM COLD SERVER STARTUP] ---------'
        ./restart_neo4j_server.sh
        time  curl -X POST $SHOW_CONTENT -H "Accept: application/json" -H "X-Stream: true"  -H "Content-type: application/json" -d @../../resources/curl/adams_friends_startingwithj.cypher   -w "total time (seconds) = %{time_total}\ntotal download size (bytes)=%{size_download}\n\n"  http://localhost:7474/db/data/cypher
        echo '-------- REST API to get All of Adams friends whose names start with J (Streaming on) [FROM WARM SERVER STARTUP] ---------'
        time  curl -X POST $SHOW_CONTENT -H "Accept: application/json" -H "X-Stream: true"  -H "Content-type: application/json" -d @../../resources/curl/adams_friends_startingwithj.cypher   -w "total time (seconds) = %{time_total}\ntotal download size (bytes)=%{size_download}\n\n"  http://localhost:7474/db/data/cypher

        echo '-------- REST API to get All of Adams friends (Streaming off) [FROM COLD SERVER STARTUP] ---------'
        ./restart_neo4j_server.sh
        time  curl -X POST $SHOW_CONTENT -H "Accept: application/json" -H "X-Stream: false" -H "Content-type: application/json" -d @../../resources/curl/adams_friends_all.cypher -w "total time (seconds) = %{time_total}\ntotal download size (bytes)=%{size_download}\n\n"  http://localhost:7474/db/data/cypher
        echo '-------- REST API to get All of Adams friends (Streaming off) [FROM WARM SERVER STARTUP] ---------'
        time  curl -X POST $SHOW_CONTENT -H "Accept: application/json" -H "X-Stream: false" -H "Content-type: application/json" -d @../../resources/curl/adams_friends_all.cypher -w "total time (seconds) = %{time_total}\ntotal download size (bytes)=%{size_download}\n\n"  http://localhost:7474/db/data/cypher


        echo '-------- REST API to get All of Adams friends (Streaming on) [FROM COLD SERVER STARTUP] ---------'
        ./restart_neo4j_server.sh
        time  curl -X POST $SHOW_CONTENT -H "Accept: application/json" -H "X-Stream: true" -H "Content-type: application/json" -d @../../resources/curl/adams_friends_all.cypher -w "total time (seconds) = %{time_total}\ntotal download size (bytes)=%{size_download}\n\n"  http://localhost:7474/db/data/cypher
        echo '-------- REST API to get All of Adams friends (Streaming on) [FROM WARM SERVER STARTUP] ---------'
        time  curl -X POST $SHOW_CONTENT -H "Accept: application/json" -H "X-Stream: true" -H "Content-type: application/json" -d @../../resources/curl/adams_friends_all.cypher -w "total time (seconds) = %{time_total}\ntotal download size (bytes)=%{size_download}\n\n"  http://localhost:7474/db/data/cypher


        echo '-------- REST API to get All of Adams friends with 100 times bigger payload returned (Streaming off) [FROM COLD SERVER STARTUP] ---------'
        ./restart_neo4j_server.sh
        time  curl -X POST $SHOW_CONTENT -H "Accept: application/json" -H "X-Stream: false" -H "Content-type: application/json" -d @../../resources/curl/adams_friends_all_x100.cypher -w "total time (seconds) = %{time_total}\ntotal download size (bytes)=%{size_download}\n\n"  http://localhost:7474/db/data/cypher
        echo '-------- REST API to get All of Adams friends with 100 times bigger payload returned (Streaming off) [FROM WARM SERVER STARTUP] ---------'
        time  curl -X POST $SHOW_CONTENT -H "Accept: application/json" -H "X-Stream: false" -H "Content-type: application/json" -d @../../resources/curl/adams_friends_all_x100.cypher -w "total time (seconds) = %{time_total}\ntotal download size (bytes)=%{size_download}\n\n"  http://localhost:7474/db/data/cypher


        echo '-------- REST API to get All of Adams friends with 100 times bigger payload returned (Streaming on) [FROM COLD SERVER STARTUP] ---------'
        ./restart_neo4j_server.sh
        time  curl -X POST $SHOW_CONTENT -H "Accept: application/json" -H "X-Stream: true" -H "Content-type: application/json" -d @../../resources/curl/adams_friends_all_x100.cypher -w "total time (seconds) = %{time_total}\ntotal download size (bytes)=%{size_download}\n\n"  http://localhost:7474/db/data/cypher
        echo '-------- REST API to get All of Adams friends with 100 times bigger payload returned (Streaming on) [FROM WARM SERVER STARTUP] ---------'
        time  curl -X POST $SHOW_CONTENT -H "Accept: application/json" -H "X-Stream: true" -H "Content-type: application/json" -d @../../resources/curl/adams_friends_all_x100.cypher -w "total time (seconds) = %{time_total}\ntotal download size (bytes)=%{size_download}\n\n"  http://localhost:7474/db/data/cypher
fi