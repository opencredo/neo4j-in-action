package com.manning.neo4jia.chapter10.serverplugin;

import com.manning.neo4jia.chapter10.JFriendGraphHelper;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.server.plugins.*;

import java.util.List;

public class JFriendNamesServerPlugin extends ServerPlugin
{

    @PluginTarget(Node.class)
    @Name( "get_friendnames_starting_with_j" )
    @Description( "Returns a list of all immediate friends whose names start with the letter J" )
    public List<String> getFriendNamesStartingWithJ( @Source Node node )
    {
        GraphDatabaseService database = node.getGraphDatabase();
        try (Transaction tx = database.beginTx()) {
            return JFriendGraphHelper.extractJFriendNamesFromRawAPI(node);
        }
    }

}
