#!/bin/bash -e

# Assumes the use of the built in bash time command NOT /usr/bin/time
# TIMEFORMAT=%R
#SHOW_CONTENT='-s -S -o /dev/null'
SHOW_CONTENT=''
RUN_OTHER_EXAMPLES=false

# ------------------------------------------------------------------------------------------------
#   A) Run scripts used to generate performance numbers used in book
# ------------------------------------------------------------------------------------------------

echo '-------- REST API to get All of Adams friends whose names start with J via unmanaged extension [FROM COLD SERVER STARTUP]  ---------'
echo '--------  Result for Table 10.4 (Row 4 [Unmanaged ext]: curl client) - COLD'
./restart_neo4j_server.sh
time  curl -X GET $SHOW_CONTENT http://localhost:7474/n4jia/unmanaged/example/user/adam001/jfriends
echo '-------- REST API to get All of Adams friends whose names start with J via unmanaged extension [FROM WARM SERVER STARTUP]  ---------'
echo '--------  Result for Table 10.4 (Row 4 [Unamanged ext]: curl client) - WARM'
time  curl -X GET $SHOW_CONTENT http://localhost:7474/n4jia/unmanaged/example/user/adam001/jfriends

# ------------------------------------------------------------------------------------------------
#   B) If desired, run additional scripts used to run other performance based tests
# ------------------------------------------------------------------------------------------------
