/**
 * Copyright 2015 Groupon.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arpnetworking.metrics.portal.expressions.impl;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import models.internal.Expression;
import models.internal.impl.DefaultExpression;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import play.test.WithApplication;

import java.util.Optional;
import java.util.UUID;

/**
 * Tests <code>DatabaseExpressionRepository</code> class.
 *
 * @author Deepika Misra (deepika at groupon dot com)
 */
public class DatabaseExpressionRepositoryTest extends WithApplication {

    @Before
    public void setup() {
        exprRepo.open();
    }

    @After
    public void teardown() {
        exprRepo.deleteAll();
        exprRepo.close();
    }

    @Test
    public void testGetForInvalidId() {
        Assert.assertFalse(exprRepo.get(UUID.randomUUID()).isPresent());
    }

    @Test
    public void testGetForValidId() {
        final UUID uuid = UUID.randomUUID();
        Assert.assertFalse(exprRepo.get(uuid).isPresent());
        final Transaction transaction = Ebean.beginTransaction();
        try {
            final models.ebean.Expression ebeanExpression = new models.ebean.Expression();
            ebeanExpression.setCluster(TEST_CLUSTER);
            ebeanExpression.setId(uuid);
            ebeanExpression.setMetric(TEST_METRIC);
            ebeanExpression.setScript(TEST_SCRIPT);
            ebeanExpression.setService(TEST_SERVICE);
            Ebean.save(ebeanExpression);
            transaction.commit();
        } finally {
            transaction.end();
        }

        final Optional<Expression> expected = exprRepo.get(uuid);
        Assert.assertTrue(expected.isPresent());
        Assert.assertEquals(uuid, expected.get().getId());
        Assert.assertEquals(TEST_CLUSTER, expected.get().getCluster());
        Assert.assertEquals(TEST_METRIC, expected.get().getMetric());
        Assert.assertEquals(TEST_SCRIPT, expected.get().getScript());
        Assert.assertEquals(TEST_SERVICE, expected.get().getService());
    }

    @Test
    public void testGetExpressionCountWithNoExpr() {
        Assert.assertEquals(0, exprRepo.getExpressionCount());
    }

    @Test
    public void testGetExpressionCountWithMultipleExpr() {
        Assert.assertEquals(0, exprRepo.getExpressionCount());
        final Transaction transaction = Ebean.beginTransaction();
        try {
            final models.ebean.Expression ebeanExpression1 = new models.ebean.Expression();
            ebeanExpression1.setCluster(TEST_CLUSTER);
            ebeanExpression1.setId(UUID.randomUUID());
            ebeanExpression1.setMetric(TEST_METRIC);
            ebeanExpression1.setScript(TEST_SCRIPT);
            ebeanExpression1.setService(TEST_SERVICE);
            Ebean.save(ebeanExpression1);
            final models.ebean.Expression ebeanExpression2 = new models.ebean.Expression();
            ebeanExpression2.setCluster(TEST_CLUSTER);
            ebeanExpression2.setId(UUID.randomUUID());
            ebeanExpression2.setMetric(TEST_METRIC);
            ebeanExpression2.setScript(TEST_SCRIPT);
            ebeanExpression2.setService(TEST_SERVICE);
            Ebean.save(ebeanExpression2);
            transaction.commit();
        } finally {
            transaction.end();
        }
        Assert.assertEquals(2, exprRepo.getExpressionCount());
    }

    @Test
    public void addOrUpdateExpressionAddCase() {
        final UUID uuid = UUID.randomUUID();
        Assert.assertFalse(exprRepo.get(uuid).isPresent());
        exprRepo.addOrUpdateExpression(exprBuilder.setId(uuid).build());
        final Optional<Expression> expected = exprRepo.get(uuid);
        Assert.assertTrue(expected.isPresent());
        Assert.assertEquals(uuid, expected.get().getId());
        Assert.assertEquals(TEST_CLUSTER, expected.get().getCluster());
        Assert.assertEquals(TEST_METRIC, expected.get().getMetric());
        Assert.assertEquals(TEST_SCRIPT, expected.get().getScript());
        Assert.assertEquals(TEST_SERVICE, expected.get().getService());
    }

    @Test
    public void addOrUpdateExpressionUpdateCase() {
        final UUID uuid = UUID.randomUUID();
        final Transaction transaction = Ebean.beginTransaction();
        try {
            final models.ebean.Expression ebeanExpression = new models.ebean.Expression();
            ebeanExpression.setCluster(TEST_CLUSTER);
            ebeanExpression.setId(uuid);
            ebeanExpression.setMetric(TEST_METRIC);
            ebeanExpression.setScript(TEST_SCRIPT);
            ebeanExpression.setService(TEST_SERVICE);
            Ebean.save(ebeanExpression);
            transaction.commit();
        } finally {
            transaction.end();
        }
        exprRepo.addOrUpdateExpression(exprBuilder
                .setId(uuid)
                .setCluster("new-cluster")
                .build());
        final Expression expected = exprRepo.get(uuid).get();
        Assert.assertEquals("new-cluster", expected.getCluster());
        Assert.assertEquals(TEST_METRIC, expected.getMetric());
        Assert.assertEquals(TEST_SCRIPT, expected.getScript());
        Assert.assertEquals(TEST_SERVICE, expected.getService());
    }

    private final DatabaseExpressionRepository.ExpressionQueryGenerator queryGenerator = new DatabaseExpressionRepository.GenericQueryGenerator();
    private final DatabaseExpressionRepository exprRepo = new DatabaseExpressionRepository(queryGenerator);

    private static final String TEST_CLUSTER = "test-cluster";
    private static final String TEST_METRIC = "test-metric";
    private static final String TEST_SCRIPT = "test-script";
    private static final String TEST_SERVICE = "test-service";
    private final DefaultExpression.Builder exprBuilder = new DefaultExpression.Builder()
            .setId(UUID.randomUUID())
            .setCluster(TEST_CLUSTER)
            .setMetric(TEST_METRIC)
            .setScript(TEST_SCRIPT)
            .setService(TEST_SERVICE);
}
