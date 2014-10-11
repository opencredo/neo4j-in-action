package com.manning.neo4jia.chapter10;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.rest.graphdb.RequestResult;
import org.neo4j.rest.graphdb.batch.CypherResult;
import org.neo4j.rest.graphdb.index.RestIndex;
import org.neo4j.rest.graphdb.services.RequestType;
import org.neo4j.rest.graphdb.util.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Contains common setup for the various timed server call demos/tests.
 */
public abstract class AbstractTimedServerCallsTests extends AbstractPerformanceTests {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTimedServerCallsTests.class);
    static final RelationshipType FRIEND_RELTYPE = DynamicRelationshipType.withName("IS_FRIEND_OF");
    static final int NUM_FRIENDS = 600;
    static final int EVERY_X_FRIEND_START_WITH_J = 40;
    static final int NUM_J_FRIENDS = NUM_FRIENDS / EVERY_X_FRIEND_START_WITH_J;
    static final String serverBaseUrl = "http://localhost:7474/db/data";

    protected String userIdIndexKey = "userId";
    protected String userIdIndexName = "userids";
    protected String adamName   = "Adam";
    protected String adamUserId = "adam001";
    protected RestIndex<Node> index4userIds;
    protected Node adamNode;
    protected Map<String, Object> adamLookupProps;
    protected Map<String, Object> dummyNodeLookupProps;

    @Before
    @Override
    public void testSetup() {
        super.testSetup();
        getOrCreateAdamAndFriendsWithRawRESTApi();
    }

    private Node createNewUser(String name, String userId) {
        Map<String,Object> props = new HashMap<String,Object>();
        props.put("name", name);
        props.put(userIdIndexKey, userId);
        Node node = restAPI.createNode(props);
        index4userIds.add(node, userIdIndexKey, userId);
        return node;
    }

    protected void getOrCreateAdamAndFriendsWithRawRESTApi() {

        logger.info("Determining whether data is setup or needs to be created ... ");
        System.setProperty(Config.CONFIG_STREAM,"false");
        System.setProperty(Config.CONFIG_BATCH_TRANSACTION,"false");

        index4userIds = restAPI.index().forNodes(userIdIndexName);
        IndexHits<Node> adamsInSystem = index4userIds.get(userIdIndexKey, adamUserId);
        switch (adamsInSystem.size()) {
            case 0  :  createAdamAndFriendsWithRawRESTApi();
                       break;
            case 1  :  adamNode = adamsInSystem.getSingle();
                       logger.info("Adam found! Node id = " + adamNode.getId() );
                       break;
            default : throw new RuntimeException("Unexpected number of Adams (" + adamsInSystem.size() + ") found in the system");
        }

        adamLookupProps = new HashMap<String,Object>();
        adamLookupProps.put("lookupId",adamUserId);

        dummyNodeLookupProps = new HashMap<String,Object>();
        dummyNodeLookupProps.put("lookupId","dummy-does-not-exist");
    }

    private void createAdamAndFriendsWithRawRESTApi() {

        logger.info("No Adam user found to exist, creating Adam with his friends");
        int beforeCreationNumNodes = countExistingNodes();
        createNewUser(adamName,adamUserId);
        adamNode = index4userIds.get(userIdIndexKey,adamUserId).getSingle();
        for (int i=1; i <= NUM_FRIENDS; i++) {
            String friendName = (i%EVERY_X_FRIEND_START_WITH_J == 0) ? "JFriend " + i :  "NonJFriend " + i;
            Node friendNode = createNewUser(friendName,"friend" + i);
            restAPI.createRelationship(adamNode,friendNode, FRIEND_RELTYPE, new HashMap());
        }
        int afterCreationNumNodes = countExistingNodes();
        assertNumberOfNewNodesCreated(beforeCreationNumNodes,afterCreationNumNodes,NUM_FRIENDS + 1);
    }

    protected void timeFindJFriendsViaUnmanagedExt(boolean bStreamOn) throws Exception {

        String streamOn = Boolean.toString(bStreamOn);
        System.setProperty(Config.CONFIG_STREAM,streamOn);
        StopWatch stopWatch = new StopWatch("timeFindJFriendsViaUnmanagedExt-"+ streamOn);
        stopWatch.start();

        // 1 x REST API network call made ...
        RequestResult result = restAPI.execute(RequestType.GET,
                "http://localhost:7474/n4jia/unmanaged/example/user/"+adamUserId+"/jfriends", null);
        String response = result.getText();
        stopWatch.stop();

        ObjectMapper mapper = new ObjectMapper();
        List names = mapper.readValue(response, List.class);
        logger.info("response = " + response);
        assertEquals("Unexpected custom number of json results ", NUM_J_FRIENDS, names.size());  // these are the friend names
        logger.info(stopWatch.shortSummary());

    }

    protected void timeFindJFriendsViaServerPluginCall(boolean bStreamOn) {

        String streamOn = Boolean.toString(bStreamOn);
        System.setProperty(Config.CONFIG_STREAM,streamOn);
        StopWatch stopWatch = new StopWatch("timeFindJFriendsViaServerPluginCall-"+ streamOn);
        stopWatch.start();

        // 1 x REST API network call made ...
        RequestResult result = restAPI.execute(RequestType.POST,
                serverBaseUrl + "/ext/JFriendNamesServerPlugin/node/" + adamNode.getId() + "/get_friendnames_starting_with_j", null);


        logger.info("result.toEntity()=" + result.toEntity());
        List<String> names = (List<String>)result.toEntity();
        stopWatch.stop();

        assertEquals( "Unexpected num data names returned" , NUM_J_FRIENDS ,names.size() );  // these are the friend names
        logger.info(stopWatch.shortSummary());

    }

    protected void timeFindJFriendsInOneCypherCall(boolean bStreamOn) {

        String streamOn = Boolean.toString(bStreamOn);
        System.setProperty(Config.CONFIG_STREAM,streamOn);
        String cypherQuery =
                "START thenode=node:userids(userId={lookupId}) " +
                        "MATCH thenode-[:IS_FRIEND_OF]-friend " +
                        "WHERE friend.name =~ 'J.*' " +
                        "RETURN friend.name ";


        // Make an initial call to ensure that our timed call does not include
        // any potential time taken to store the cypher query in a query cache
        restAPI.query(cypherQuery, dummyNodeLookupProps);

        StopWatch stopWatch = new StopWatch("timeFindJFriendsInOneCypherCallStreaming-"+ streamOn);
        stopWatch.start();
        CypherResult result = restAPI.query(cypherQuery, adamLookupProps);
        Map<?, ?> resultMap = result.asMap();
        assertEquals( "Unexpected return format" , 2 ,resultMap.size() );
        List columns = (List)resultMap.get("columns");
        List data = (List)resultMap.get("data");
        stopWatch.stop();
        assertEquals( "Unexpected num columns returned"     , 1 ,columns.size() );
        assertEquals( "Unexpected column returned"          , "friend.name" ,columns.get(0) );
        assertEquals( "Unexpected num data values returned" , NUM_J_FRIENDS ,data.size() );  // these are the friend names
        logger.info(stopWatch.shortSummary());

    }

    protected void timeFindJFriendsUsingHypermediaDrivenRESTAPI(boolean bStreamOn) {

        String streamOn = Boolean.toString(bStreamOn);
        System.setProperty(Config.CONFIG_STREAM,streamOn);
        System.setProperty(Config.CONFIG_BATCH_TRANSACTION,"false");

        List<String> jNamedFriends = new ArrayList<String>();
        StopWatch stopWatch = new StopWatch("timeFindJFriendsUsingHypermediaDrivenRESTAPI-Streaming-" + streamOn);
        stopWatch.start();

        // 1 x REST API call to get details about Adam
        Node retrievedAdamNode = restAPI.getNodeById(adamNode.getId());
        assertNotNull("Could not retrieve Adam node", retrievedAdamNode);

        // 1 x REST API call to retrieve all the IS_FRIEND_OF relationships for Adam
        Iterable<Relationship> rels = retrievedAdamNode.getRelationships(FRIEND_RELTYPE, Direction.BOTH);

        for (Relationship relationship: rels) {
            // For each relationship (600) make 600 X REST API calls to
            // get the associated properties of the friend node
            Node aFriendNode = relationship.getEndNode();
            String friendName = (String)aFriendNode.getProperty("name");
            if (friendName.startsWith("J")) { jNamedFriends.add(friendName); }
        }
        stopWatch.stop();
        assertEquals( "Unexpected number of friends with names starting with J found" , NUM_J_FRIENDS , jNamedFriends.size() );
        logger.info(stopWatch.shortSummary());
    }


    protected List extractData(Map<?, ?> result, int numColsExpected, String expectedColumnName) {
        assertEquals( "Unexpected return format" , 2 ,result.size() );
        List columns = (List)result.get("columns");
        List data = (List)result.get("data");
        assertEquals( "Unexpected num columns returned"     , numColsExpected ,columns.size() );
        assertEquals( "Unexpected column returned"          , expectedColumnName ,columns.get(0) );
        return data;
    }

    protected String getCypherQuery(int factorSizeIncrease) {
        String returnQueryStr = "";
        for (int a = 1; a <= factorSizeIncrease; a++) {
            if (a != 1 && a <= factorSizeIncrease)  {
                returnQueryStr += " , ";
            }
            returnQueryStr += " friend";
        }
        return "START thenode=node:userids(userId={lookupId}) " +
                "MATCH thenode-[:IS_FRIEND_OF]-friend " +
                "RETURN " + returnQueryStr;
    }



}
