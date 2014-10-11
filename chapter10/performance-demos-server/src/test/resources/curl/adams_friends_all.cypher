{ "query" :
    "MATCH ( x:Person { userId: { lookupId } } )-[r:IS_FRIEND_OF]-(friend)
     RETURN friend ",
  "params" : { "userId" : "adam001" }
}
