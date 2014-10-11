#!/bin/bash -e

# Running each test from this script ensures that the JVM is started up fresh in each case, and that there
# is no leftover data loaded into the JVM memory from previous tests etc
#
# To only get the stopwatch details, recommended way to call script is via
# ./curl_adams_friends.sh | grep StopWatch
#

./curl_adams_friends_via_rawapi.sh
./curl_adams_friends_via_cypher.sh
./curl_adams_friends_via_serverplugin.sh
./curl_adams_friends_via_unmanagedext.sh
