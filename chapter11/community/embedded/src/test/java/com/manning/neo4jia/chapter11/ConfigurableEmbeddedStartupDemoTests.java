package com.manning.neo4jia.chapter11;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.assertNotNull;

public class ConfigurableEmbeddedStartupDemoTests {


    private GraphDatabaseService graphDatabaseService;
    private String embeddedConfigFilename;


    @Before
    public void setUp() throws Exception{
        URL url = this.getClass().getClassLoader().getResource("embedded-neo4j.properties");
        if (url == null) {
            throw new RuntimeException("Problem occurred trying to read config file to setup tests");
        }
        File file = new File(url.toURI());
        embeddedConfigFilename = file.getAbsolutePath();
    }

    @After
    public void tearDown() {
        if (graphDatabaseService != null) {
            graphDatabaseService.shutdown();
        }
    }

    @Test
    public void createEmbeddedGraphDBWithConfigLoadedFromPropertiesFile() throws Exception {

        graphDatabaseService = new GraphDatabaseFactory()
                .newEmbeddedDatabaseBuilder("target/database/chapter11")
                .loadPropertiesFromFile(embeddedConfigFilename)
                        .newGraphDatabase();

        assertNotNull("Unable to start graph database with embedded config file", graphDatabaseService);

    }



}
