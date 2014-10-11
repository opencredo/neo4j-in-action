package com.manning.neo4jia.chapter06;

import com.manning.neo4jia.chapter03.UsersAndMovies_20Style;
import org.apache.commons.io.FileUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * @author Aleksa Vukotic
 */
public class DataCreator {

    private static Logger logger = LoggerFactory.getLogger(DataCreator.class);
    public static final String DEFAULT_NEO4J_STORE_DIR = "/tmp/neo4j-chapter06";

    private String storeDir = DEFAULT_NEO4J_STORE_DIR;

    GraphDatabaseService graphDb =
            new GraphDatabaseFactory().newEmbeddedDatabase(storeDir);


    public static void main(String[] args) throws IOException {
        new DataCreator().recreateData();
        logger.info("main finished");
    }


    public void initEmptyGraph() throws IOException {
        if (graphDb != null) {
            graphDb.shutdown();
        }
        FileUtils.deleteDirectory(new File(storeDir));
        graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(storeDir);


    }

    public void recreateData() throws IOException {
        initEmptyGraph();
        UsersAndMovies_20Style usersAndMovies = new UsersAndMovies_20Style(graphDb);
        usersAndMovies.createSingleUser();
        usersAndMovies.createMultipleUsersInSingleTransaction();
        usersAndMovies.createSimpleRelationshipsBetweenUsers();
        usersAndMovies.addPropertiesToUserNodes();
        usersAndMovies.addMorePropertiesToUsers();
        usersAndMovies.createMoviesNodes();
        usersAndMovies.addTypePropertiesToNodes();
        usersAndMovies.addPropertiesToRelationships();
        indexAllUsers();
        graphDb.shutdown();
    }

    public void indexAllUsers() {
        Transaction tx = graphDb.beginTx();
        try {
            for (Node n : graphDb.getAllNodes()) {
                if ("user".equalsIgnoreCase((String) n.getProperty("type", "UNKNOWN"))) {
                    Index<Node> usersIndex = graphDb.index().forNodes("users");
                    usersIndex.remove(n);
                    if (n.hasProperty("name")) {
                        usersIndex.add(n, "name", n.getProperty("name"));
                    }
                    if (n.hasProperty("year_of_birth")) {
                        usersIndex.add(n, "year_of_birth", n.getProperty("year_of_birth"));
                    }


                }
            }
            tx.success();
        } finally {
            tx.finish();
        }
    }

    public void setStoreDir(String storeDir) {
        this.storeDir = storeDir;
    }
}
