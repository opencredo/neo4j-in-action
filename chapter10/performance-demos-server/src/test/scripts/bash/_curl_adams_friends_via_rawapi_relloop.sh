#!/bin/bash -e

SHOW_CONTENT='-s -S -o /dev/null'


echo '* * * Going through each IF_FRIEND_OF relationshipand getting properties of end node ------------- '
for num in {1..600}
do
  # 2. For each relationship returned above (for now our data has been setup such that all of Adams friends are nodes 2 -> 601)
  #    Get the properties for this node
  curl -X GET $SHOW_CONTENT -H "Accept: application/json" -H "X-Stream: false" -H "Content-type: application/json"  http://localhost:7474/db/data/node/$num/properties

  # 3. Perform some kind of filtering to only show the friends whose names start with J
  #    Pretend this is being done
done
