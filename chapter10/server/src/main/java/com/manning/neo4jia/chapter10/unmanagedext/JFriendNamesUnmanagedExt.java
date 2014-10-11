package com.manning.neo4jia.chapter10.unmanagedext;

import com.manning.neo4jia.chapter10.JFriendGraphHelper;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.*;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.util.List;

/**
 * Provides An example of an Unmanaged extension which is responsible for returning all the
 * names of friends starting with a "J" for the provided userId. This is used for a
 * comparison against other techniques for performance measuring metrics as well.
 */
@Path( "/example" )
public class JFriendNamesUnmanagedExt {

    private final GraphDatabaseService database;
    private static final Label PERSON = DynamicLabel.label("Person");

    public JFriendNamesUnmanagedExt( @Context GraphDatabaseService database )
    {
        this.database = database;
    }

    @GET
    @Produces( MediaType.TEXT_PLAIN )
    @Path( "/user/{userId}/jfriends-with-custom-response" )
    public Response getFriendNamesStartingWithJAsTextPlain( @PathParam( "userId" ) String userId )
    {
        List<String> names = null;
        try (Transaction tx = database.beginTx()) {
            Node theUser = database.findNodesByLabelAndProperty(
                            PERSON, "userId", userId)
                            .iterator().next();
            names = JFriendGraphHelper.extractJFriendNamesFromRawAPI(theUser);
        }

        // Note custom made up response: This could be anything including binary data
        // such as a generated PDF etc. Provided the MediaType is set appropriately, you
        // can return the data in whatever format you choose
        return Response.status( Status.OK ).entity(
                ( "custom-response-with-jfriendnames=> " + names ).getBytes() ).build();
    }

    @GET
    @Produces( MediaType.APPLICATION_JSON )
    @Path( "/user/{userId}/jfriends" )
    public Response getFriendNamesStartingWithJAsJSON( @PathParam( "userId" ) String userId )
            throws IOException
    {
        List<String> names = null;
        try (Transaction tx = database.beginTx()) {
            Node theUser = database.findNodesByLabelAndProperty(
                    PERSON, "userId", userId)
                    .iterator().next();
            names = JFriendGraphHelper.extractJFriendNamesFromRawAPI(theUser);
        }

        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(names);
        return Response.status( Status.OK ).entity(jsonString ).build();
    }
}
