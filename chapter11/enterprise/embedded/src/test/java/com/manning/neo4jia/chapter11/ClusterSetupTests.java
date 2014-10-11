package com.manning.neo4jia.chapter11;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.HighlyAvailableGraphDatabaseFactory;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.assertNotNull;

public class ClusterSetupTests {


    private GraphDatabaseService graphDatabaseService;
    private String machineAFilename;


    @Before
    public void setUp() throws Exception{
        URL url = this.getClass().getClassLoader().getResource("single-machine-cluster-neo4j.properties");
        if (url == null) {
            throw new RuntimeException("Problem occurred trying to read config file to setup tests");
        }
        File file = new File(url.toURI());
        machineAFilename = file.getAbsolutePath();
    }

    @After
    public void tearDown() {
        if (graphDatabaseService != null) {
            graphDatabaseService.shutdown();
        }
    }

    @Test
    public void startupEmbeddedCluster() throws Exception {

        graphDatabaseService = new HighlyAvailableGraphDatabaseFactory()
                .newHighlyAvailableDatabaseBuilder("target/database/chapter11/enterprise/embedded/machineA")
                .loadPropertiesFromFile(machineAFilename)
                        .newGraphDatabase();

        assertNotNull("Unable to start graph database with embedded config file", graphDatabaseService);

    }



}
