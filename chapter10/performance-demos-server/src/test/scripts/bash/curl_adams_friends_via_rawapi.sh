#!/bin/bash -e

# Assumes the use of the built in bash time command NOT /usr/bin/time
#SHOW_CONTENT=''
SHOW_CONTENT='-s -S -o /dev/null'
RUN_OTHER_EXAMPLES=false

# ------------------------------------------------------------------------------------------------
#   A) Run scripts used to generate performance numbers used in book
# ------------------------------------------------------------------------------------------------

echo '------------------------------- Scenario 1 : Raw API with Streaming Off [FROM COLD SERVER STARTUP] ------------------------- '
echo '--------  Result for Table 10.4 (Row 1 [RAW API]: curl client) - COLD'
./restart_neo4j_server.sh
time ./_curl_adams_friends_via_rawapi_allcalls.sh
echo '*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*'


echo '------------------------------- Scenario 1 : Raw API with Streaming Off [FROM WARM SERVER STARTUP] ------------------------- '
echo '--------  Result for Table 10.4 (Row 1 [RAW API]: curl client) - WARM'
time ./_curl_adams_friends_via_rawapi_allcalls.sh
echo '*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*'


# ------------------------------------------------------------------------------------------------
#   B) If desired, run additional scripts used to run other performance based tests
# ------------------------------------------------------------------------------------------------
if [ $RUN_OTHER_EXAMPLES = "true" ]; then

        echo '\n\n------------------------------- Scenario 2 : Raw API with Streaming On [FROM COLD SERVER STARTUP] ------------------------- '
        ./restart_neo4j_server.sh
        echo '* * * sample time taken for 1 x properties call (Stream On)'
        time curl -X GET $SHOW_CONTENT -H "Accept: application/json;stream=true" -H "X-Stream: true" -H "Content-type: application/json" -w "total time (seconds) = %{time_total}\ntotal download size (bytes)=%{size_download}\n\n"  http://localhost:7474/db/data/node/2/properties
        # 0. Lookup Adams node from the index
        #      curl -X GET $SHOW_CONTENT -H "Accept: application/json" -H "X-Stream: false" -H "Content-type: application/json" http://localhost:7474/db/data/index/node/usernames/name/Adam
        #    For this particular case we know Adam is node id 0 and all his friends are 1 to 600 .... so we are hard coding
        echo '* * * Getting all IF_FRIEND_OF relationships ------------- '
        # 1. Get all of the IS_FRIEND_OF relationships data and next set of hyperlinks to associated data for Adam
        time curl -X GET $SHOW_CONTENT -H "Accept: application/json;stream=true" -H "X-Stream: true" -H "Content-type: application/json" http://localhost:7474/db/data/node/0/relationships/all/IS_FRIEND_OF
        time ./_curl_adams_friends_via_rawapi_relloop.sh

        echo '\n\n------------------------------- Scenario 2 : Raw API with Streaming On [FROM WARM SERVER STARTUP] ------------------------- '
        echo '* * * sample time taken for 1 x properties call (Stream On)'
        time curl -X GET $SHOW_CONTENT -H "Accept: application/json;stream=true" -H "X-Stream: true" -H "Content-type: application/json" -w "total time (seconds) = %{time_total}\ntotal download size (bytes)=%{size_download}\n\n"  http://localhost:7474/db/data/node/2/properties
        # 0. Lookup Adams node from the index
        #      curl -X GET $SHOW_CONTENT -H "Accept: application/json" -H "X-Stream: false" -H "Content-type: application/json" http://localhost:7474/db/data/index/node/usernames/name/Adam
        #    For this particular case we know Adam is node id 0 and all his friends are 1 to 600 .... so we are hard coding
        echo '* * * Getting all IF_FRIEND_OF relationships ------------- '
        # 1. Get all of the IS_FRIEND_OF relationships data and next set of hyperlinks to associated data for Adam
        time curl -X GET $SHOW_CONTENT -H "Accept: application/json;stream=true" -H "X-Stream: true" -H "Content-type: application/json" http://localhost:7474/db/data/node/0/relationships/all/IS_FRIEND_OF
        time ./_curl_adams_friends_via_rawapi_relloop.sh

fi