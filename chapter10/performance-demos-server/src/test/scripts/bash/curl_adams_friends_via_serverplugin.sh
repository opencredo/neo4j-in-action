#!/bin/bash -e

# Assumes the use of the built in bash time command NOT /usr/bin/time
# TIMEFORMAT=%R
#SHOW_CONTENT='-s -S -o /dev/null'
SHOW_CONTENT=''
RUN_OTHER_EXAMPLES=false

# ------------------------------------------------------------------------------------------------
#   A) Run scripts used to generate performance numbers used in book
# ------------------------------------------------------------------------------------------------

echo '-------- REST API to get All of Adams friends whose names start with J via server plugin (Streaming off) [FROM COLD SERVER STARTUP]  ---------'
echo '--------  Result for Table 10.4 (Row 3 [Server Plugin]: curl client) - COLD'
./restart_neo4j_server.sh
time  curl -X POST $SHOW_CONTENT -H "Accept: application/json" -H "X-Stream: false" -H "Content-type: application/json"  http://localhost:7474/db/data/ext/JFriendNamesServerPlugin/node/0/get_friendnames_starting_with_j
echo '-------- REST API to get All of Adams friends whose names start with J via server plugin (Streaming off) [FROM WARM SERVER STARTUP]  ---------'
echo '--------  Result for Table 10.4 (Row 3 [Server Plugin]: curl client) - WARM'
time  curl -X POST $SHOW_CONTENT -H "Accept: application/json" -H "X-Stream: false" -H "Content-type: application/json"  http://localhost:7474/db/data/ext/JFriendNamesServerPlugin/node/0/get_friendnames_starting_with_j

# ------------------------------------------------------------------------------------------------
#   B) If desired, run additional scripts used to run other performance based tests
# ------------------------------------------------------------------------------------------------
