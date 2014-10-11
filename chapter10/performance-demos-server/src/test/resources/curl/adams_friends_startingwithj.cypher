{ "query" : 
      "MATCH ( x:Person { userId: { lookupId } } )-[r:IS_FRIEND_OF]-(friend)
         WHERE friend.name =~ {name2find} 
         RETURN friend.name ", 
  "params" : { "lookupId" : "adam001",  "name2find"  : "J.*" } 
} 