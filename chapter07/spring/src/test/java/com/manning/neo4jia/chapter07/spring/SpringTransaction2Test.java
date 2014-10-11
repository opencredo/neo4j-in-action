package com.manning.neo4jia.chapter07.spring;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotInTransactionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertNotNull;

/**
 *
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/SpringTransactionTest-context.xml"})
public class SpringTransaction2Test {

    @Autowired
    private APretendService pretendService;

    @Test(expected = NotInTransactionException.class)
    public void testNodeCreationOutsideOfTransactionShouldFail() {
        pretendService.createPlainNode("John", 34);
    }

    @Test
    public void testNodeCreationWithSpringAnnotatedTxOnServiceMethodShouldSucceed() throws Exception {
        Node node = pretendService.createNodeViaAnnotatedMethod("John", 34);
        assertNotNull(node);
    }

    @Test
    @Transactional
    public void testNodeCreationWithSpringAnnotatedTxOnTestMethodShouldSucceed() throws Exception {
        Node node = pretendService.createPlainNode("John", 34);
        assertNotNull(node);
    }

}
