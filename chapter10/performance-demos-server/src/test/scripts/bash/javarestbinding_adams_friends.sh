#!/bin/bash -e

# Running each test from this script ensures that the JVM is started up fresh in each case, and that there
# is no leftover data loaded into the JVM memory from previous tests etc
#
# To only get the stopwatch details, recommended way to call script is via
# ./javarestbinding_adams_friends.sh | grep StopWatch
#
# Note: These scripts will actually use maven to run the various server based demo tests defined
#

./javarestbinding_adams_friends_via_rawapi.sh
./javarestbinding_adams_friends_via_cypher.sh
./javarestbinding_adams_friends_via_serverplugin.sh
./javarestbinding_adams_friends_via_unmanagedext.sh
