



# ---------------- Current number of nodes in DB ---------------

curl -X POST -d '{ "query" : "MATCH n RETURN COUNT(n) " } '                                 -H "Accept: application/json" -H "Content-type: application/json"                                 http://localhost:7474/db/data/cypher




# ------------ Returning ALL nodes (Streaming OFF) -------------

curl -X POST -d '{ "query" : "MATCH n RETURN COUNT(n) " } '                                       -H "Accept: application/json" -H "Content-type: application/json"                                 --limit-rate 12m -H "X-Stream: false"                                                             http://localhost:7474/db/data/cypher > /dev/null




# ------------ Returning ALL nodes (Streaming ON) -------------

curl -X POST -d '{ "query" : "MATCH n RETURN COUNT(n) " } '                                       -H "Accept: application/json" -H "Content-type: application/json"                                 --limit-rate 12m -H "X-Stream: true"                                                              http://localhost:7474/db/data/cypher > /dev/null



