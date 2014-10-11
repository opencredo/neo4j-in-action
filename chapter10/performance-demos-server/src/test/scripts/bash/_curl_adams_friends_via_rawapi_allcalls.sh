#!/bin/bash -e

# Assumes the use of the built in bash time command NOT /usr/bin/time
#SHOW_CONTENT=''
SHOW_CONTENT='-s -S -o /dev/null'
RUN_OTHER_EXAMPLES=false


#    For this particular case we know Adam is node id 0 and all his friends are 1 to 600 .... so we are hard coding
echo '* * *  Getting Adams main data '
# 1. Get Adams main data
time curl -X GET $SHOW_CONTENT -H "Accept: application/json" -H "X-Stream: false" -H "Content-type: application/json" http://localhost:7474/db/data/node/0

echo '* * *  Getting all IF_FRIEND_OF relationships '
# 1. Get all of the IS_FRIEND_OF relationships data and next set of hyperlinks to associated data for Adam
time curl -X GET $SHOW_CONTENT -H "Accept: application/json" -H "X-Stream: false" -H "Content-type: application/json" http://localhost:7474/db/data/node/0/relationships/all/IS_FRIEND_OF
time ./_curl_adams_friends_via_rawapi_relloop.sh
echo '*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*'
echo '*-*-*-*-*-*                         Total Combined Timings for RAW API                              *-*-*-*-*-*'


