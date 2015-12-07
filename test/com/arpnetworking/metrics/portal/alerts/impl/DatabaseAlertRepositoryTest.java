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
package com.arpnetworking.metrics.portal.alerts.impl;

import com.arpnetworking.jackson.BuilderDeserializer;
import com.arpnetworking.jackson.ObjectMapperFactory;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import models.internal.Alert;
import models.internal.Context;
import models.internal.Operator;
import models.internal.Quantity;
import models.internal.impl.DefaultAlert;
import models.internal.impl.DefaultQuantity;
import org.joda.time.Period;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import play.test.WithApplication;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * TODO: Class Description.
 *
 * @author Deepika Misra (deepika at groupon dot com)
 */
public class DatabaseAlertRepositoryTest extends WithApplication {

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
    public void testGetForValidId() throws JsonProcessingException {
        final UUID uuid = UUID.randomUUID();
        Assert.assertFalse(exprRepo.get(uuid).isPresent());
        final Transaction transaction = Ebean.beginTransaction();
        try {
            final models.ebean.Alert ebeanAlert = new models.ebean.Alert();
            ebeanAlert.setExtensions(TEST_EXTENSIONS.toString());
            ebeanAlert.setName(TEST_NAME);
            ebeanAlert.setOperator(TEST_OPERATOR.toString());
            ebeanAlert.setPeriod(TEST_PERIOD.toString());
            ebeanAlert.setStatistic(TEST_STATISTIC);
            ebeanAlert.setQuantity(OBJECT_MAPPER.writeValueAsString(TEST_VALUE));
            ebeanAlert.setCluster(TEST_CLUSTER);
            ebeanAlert.setId(uuid);
            ebeanAlert.setMetric(TEST_METRIC);
            ebeanAlert.setContext(TEST_CONTEXT.toString());
            ebeanAlert.setService(TEST_SERVICE);
            Ebean.save(ebeanAlert);
            transaction.commit();
        } finally {
            transaction.end();
        }

        final Optional<Alert> expected = exprRepo.get(uuid);
        Assert.assertTrue(expected.isPresent());
        Assert.assertEquals(uuid, expected.get().getId());
        Assert.assertEquals(TEST_CLUSTER, expected.get().getCluster());
        Assert.assertEquals(TEST_METRIC, expected.get().getMetric());
        Assert.assertEquals(TEST_EXTENSIONS, expected.get().getExtensions());
        Assert.assertEquals(TEST_SERVICE, expected.get().getService());
        Assert.assertEquals(TEST_NAME, expected.get().getName());
        Assert.assertEquals(TEST_OPERATOR, expected.get().getOperator());
        Assert.assertEquals(TEST_PERIOD, expected.get().getPeriod());
        Assert.assertEquals(TEST_STATISTIC, expected.get().getStatistic());
        Assert.assertEquals(TEST_VALUE, expected.get().getValue());
        Assert.assertEquals(TEST_CONTEXT, expected.get().getContext());
    }

    @Test
    public void testGetAlertCountWithNoExpr() {
        Assert.assertEquals(0, exprRepo.getAlertCount());
    }

    @Test
    public void testGetAlertCountWithMultipleExpr() throws JsonProcessingException {
        Assert.assertEquals(0, exprRepo.getAlertCount());
        final Transaction transaction = Ebean.beginTransaction();
        try {
            final models.ebean.Alert ebeanAlert1 = new models.ebean.Alert();
            ebeanAlert1.setExtensions(TEST_EXTENSIONS.toString());
            ebeanAlert1.setName(TEST_NAME);
            ebeanAlert1.setOperator(TEST_OPERATOR.toString());
            ebeanAlert1.setPeriod(TEST_PERIOD.toString());
            ebeanAlert1.setStatistic(TEST_STATISTIC);
            ebeanAlert1.setQuantity(OBJECT_MAPPER.writeValueAsString(TEST_VALUE));
            ebeanAlert1.setCluster(TEST_CLUSTER);
            ebeanAlert1.setId(UUID.randomUUID());
            ebeanAlert1.setMetric(TEST_METRIC);
            ebeanAlert1.setContext(TEST_CONTEXT.toString());
            ebeanAlert1.setService(TEST_SERVICE);
            Ebean.save(ebeanAlert1);
            final models.ebean.Alert ebeanAlert2 = new models.ebean.Alert();
            ebeanAlert2.setExtensions(TEST_EXTENSIONS.toString());
            ebeanAlert2.setName(TEST_NAME);
            ebeanAlert2.setOperator(TEST_OPERATOR.toString());
            ebeanAlert2.setPeriod(TEST_PERIOD.toString());
            ebeanAlert2.setStatistic(TEST_STATISTIC);
            ebeanAlert2.setQuantity(OBJECT_MAPPER.writeValueAsString(TEST_VALUE));
            ebeanAlert2.setCluster(TEST_CLUSTER);
            ebeanAlert2.setId(UUID.randomUUID());
            ebeanAlert2.setMetric(TEST_METRIC);
            ebeanAlert2.setContext(TEST_CONTEXT.toString());
            ebeanAlert2.setService(TEST_SERVICE);
            Ebean.save(ebeanAlert2);
            transaction.commit();
        } finally {
            transaction.end();
        }
        Assert.assertEquals(2, exprRepo.getAlertCount());
    }

    @Test
    public void addOrUpdateAlertAddCase() {
        final UUID uuid = UUID.randomUUID();
        Assert.assertFalse(exprRepo.get(uuid).isPresent());
        exprRepo.addOrUpdateAlert(exprBuilder.setId(uuid).build());
        final Optional<Alert> expected = exprRepo.get(uuid);
        Assert.assertTrue(expected.isPresent());
        Assert.assertEquals(uuid, expected.get().getId());
        Assert.assertEquals(TEST_CLUSTER, expected.get().getCluster());
        Assert.assertEquals(TEST_METRIC, expected.get().getMetric());
        Assert.assertEquals(TEST_EXTENSIONS, expected.get().getExtensions());
        Assert.assertEquals(TEST_SERVICE, expected.get().getService());
        Assert.assertEquals(TEST_NAME, expected.get().getName());
        Assert.assertEquals(TEST_OPERATOR, expected.get().getOperator());
        Assert.assertEquals(TEST_PERIOD, expected.get().getPeriod());
        Assert.assertEquals(TEST_STATISTIC, expected.get().getStatistic());
        Assert.assertEquals(TEST_VALUE, expected.get().getValue());
        Assert.assertEquals(TEST_CONTEXT, expected.get().getContext());
    }

    @Test
    public void addOrUpdateAlertUpdateCase() throws JsonProcessingException {
        final UUID uuid = UUID.randomUUID();
        final Transaction transaction = Ebean.beginTransaction();
        try {
            final models.ebean.Alert ebeanAlert = new models.ebean.Alert();
            ebeanAlert.setExtensions(TEST_EXTENSIONS.toString());
            ebeanAlert.setName(TEST_NAME);
            ebeanAlert.setOperator(TEST_OPERATOR.toString());
            ebeanAlert.setPeriod(TEST_PERIOD.toString());
            ebeanAlert.setStatistic(TEST_STATISTIC);
            ebeanAlert.setQuantity(OBJECT_MAPPER.writeValueAsString(TEST_VALUE));
            ebeanAlert.setCluster(TEST_CLUSTER);
            ebeanAlert.setId(uuid);
            ebeanAlert.setMetric(TEST_METRIC);
            ebeanAlert.setContext(TEST_CONTEXT.toString());
            ebeanAlert.setService(TEST_SERVICE);
            Ebean.save(ebeanAlert);
            transaction.commit();
        } finally {
            transaction.end();
        }
        exprRepo.addOrUpdateAlert(exprBuilder
                .setId(uuid)
                .setCluster("new-cluster")
                .build());
        final Optional<Alert> expected = exprRepo.get(uuid);
        Assert.assertTrue(expected.isPresent());
        Assert.assertEquals(uuid, expected.get().getId());
        Assert.assertEquals("new-cluster", expected.get().getCluster());
        Assert.assertEquals(TEST_METRIC, expected.get().getMetric());
        Assert.assertEquals(TEST_EXTENSIONS, expected.get().getExtensions());
        Assert.assertEquals(TEST_SERVICE, expected.get().getService());
        Assert.assertEquals(TEST_NAME, expected.get().getName());
        Assert.assertEquals(TEST_OPERATOR, expected.get().getOperator());
        Assert.assertEquals(TEST_PERIOD, expected.get().getPeriod());
        Assert.assertEquals(TEST_STATISTIC, expected.get().getStatistic());
        Assert.assertEquals(TEST_VALUE, expected.get().getValue());
        Assert.assertEquals(TEST_CONTEXT, expected.get().getContext());
    }

    private final DatabaseAlertRepository.AlertQueryGenerator queryGenerator = new DatabaseAlertRepository.GenericQueryGenerator();
    private final DatabaseAlertRepository exprRepo = new DatabaseAlertRepository(queryGenerator);
    private final DefaultAlert.Builder exprBuilder = new DefaultAlert.Builder()
            .setId(UUID.randomUUID())
            .setCluster(TEST_CLUSTER)
            .setMetric(TEST_METRIC)
            .setContext(TEST_CONTEXT)
            .setService(TEST_SERVICE)
            .setExtensions(TEST_EXTENSIONS)
            .setName(TEST_NAME)
            .setOperator(TEST_OPERATOR)
            .setPeriod(TEST_PERIOD)
            .setStatistic(TEST_STATISTIC)
            .setValue(TEST_VALUE);

    private static final String TEST_CLUSTER = "test-cluster";
    private static final String TEST_METRIC = "test-metric";
    private static final String TEST_SERVICE = "test-service";
    private static final Context TEST_CONTEXT = Context.CLUSTER;
    private static final Map<String, Object> TEST_EXTENSIONS = Collections.emptyMap();
    private static final String TEST_NAME = "test-name";
    private static final Operator TEST_OPERATOR = Operator.EQUAL_TO;
    private static final Period TEST_PERIOD = Period.days(1);
    private static final String TEST_STATISTIC = "test-statistic";
    private static final Quantity TEST_VALUE = new DefaultQuantity.Builder().setValue(12.0).setUnit("test-unit").build();
    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.createInstance();

    static {
        final SimpleModule module = new SimpleModule("DatabaseAlertRepositoryTest");
        module.addDeserializer(
                Quantity.class,
                BuilderDeserializer.of(DefaultQuantity.Builder.class));
        OBJECT_MAPPER.registerModule(module);
    }
}
