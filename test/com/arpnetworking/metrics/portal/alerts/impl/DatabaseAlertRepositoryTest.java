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

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;
import models.ebean.NagiosExtension;
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
 * Tests class <code>DatabaseAlertRepository</code>.
 *
 * @author Deepika Misra (deepika at groupon dot com)
 */
public class DatabaseAlertRepositoryTest extends WithApplication {

    @Before
    public void setup() {
        alertRepo.open();
    }

    @After
    public void teardown() {
        alertRepo.deleteAll();
        alertRepo.close();
    }

    @Test
    public void testGetForInvalidId() {
        Assert.assertFalse(alertRepo.get(UUID.randomUUID()).isPresent());
    }

    @Test
    public void testGetForValidId() throws JsonProcessingException {
        final UUID uuid = UUID.randomUUID();
        Assert.assertFalse(alertRepo.get(uuid).isPresent());
        final Transaction transaction = Ebean.beginTransaction();
        try {
            final models.ebean.Alert ebeanAlert = new models.ebean.Alert();
            ebeanAlert.setNagiosExtension(TEST_NAGIOS_EXTENSIONS);
            ebeanAlert.setName(TEST_NAME);
            ebeanAlert.setOperator(TEST_OPERATOR);
            ebeanAlert.setPeriod(TEST_PERIOD_IN_SECONDS);
            ebeanAlert.setStatistic(TEST_STATISTIC);
            ebeanAlert.setQuantityValue(TEST_QUANTITY_VALUE);
            ebeanAlert.setQuantityUnit(TEST_QUANTITY_UNIT);
            ebeanAlert.setCluster(TEST_CLUSTER);
            ebeanAlert.setUuid(uuid);
            ebeanAlert.setMetric(TEST_METRIC);
            ebeanAlert.setContext(TEST_CONTEXT);
            ebeanAlert.setService(TEST_SERVICE);
            Ebean.save(ebeanAlert);
            transaction.commit();
        } finally {
            transaction.end();
        }

        final Optional<Alert> expected = alertRepo.get(uuid);
        Assert.assertTrue(expected.isPresent());
        Assert.assertEquals(uuid, expected.get().getId());
        Assert.assertEquals(TEST_CLUSTER, expected.get().getCluster());
        Assert.assertEquals(TEST_METRIC, expected.get().getMetric());
        Assert.assertEquals(TEST_EXTENSIONS, expected.get().getExtensions());
        Assert.assertEquals(TEST_SERVICE, expected.get().getService());
        Assert.assertEquals(TEST_NAME, expected.get().getName());
        Assert.assertEquals(TEST_OPERATOR, expected.get().getOperator());
        Assert.assertEquals(Period.seconds(TEST_PERIOD_IN_SECONDS), expected.get().getPeriod());
        Assert.assertEquals(TEST_STATISTIC, expected.get().getStatistic());
        Assert.assertEquals(TEST_QUANTITY, expected.get().getValue());
        Assert.assertEquals(TEST_CONTEXT, expected.get().getContext());
    }

    @Test
    public void testGetAlertCountWithNoExpr() {
        Assert.assertEquals(0, alertRepo.getAlertCount());
    }

    @Test
    public void testGetAlertCountWithMultipleExpr() throws JsonProcessingException {
        Assert.assertEquals(0, alertRepo.getAlertCount());
        final Transaction transaction = Ebean.beginTransaction();
        try {
            final models.ebean.Alert ebeanAlert1 = new models.ebean.Alert();
            ebeanAlert1.setNagiosExtension(TEST_NAGIOS_EXTENSIONS);
            ebeanAlert1.setName(TEST_NAME);
            ebeanAlert1.setOperator(TEST_OPERATOR);
            ebeanAlert1.setPeriod(TEST_PERIOD_IN_SECONDS);
            ebeanAlert1.setStatistic(TEST_STATISTIC);
            ebeanAlert1.setQuantityValue(TEST_QUANTITY_VALUE);
            ebeanAlert1.setQuantityUnit(TEST_QUANTITY_UNIT);
            ebeanAlert1.setCluster(TEST_CLUSTER);
            ebeanAlert1.setUuid(UUID.randomUUID());
            ebeanAlert1.setMetric(TEST_METRIC);
            ebeanAlert1.setContext(TEST_CONTEXT);
            ebeanAlert1.setService(TEST_SERVICE);
            Ebean.save(ebeanAlert1);
            final models.ebean.Alert ebeanAlert2 = new models.ebean.Alert();
            ebeanAlert2.setNagiosExtension(TEST_NAGIOS_EXTENSIONS);
            ebeanAlert2.setName(TEST_NAME);
            ebeanAlert2.setOperator(TEST_OPERATOR);
            ebeanAlert2.setPeriod(TEST_PERIOD_IN_SECONDS);
            ebeanAlert2.setStatistic(TEST_STATISTIC);
            ebeanAlert2.setQuantityValue(TEST_QUANTITY_VALUE);
            ebeanAlert2.setQuantityUnit(TEST_QUANTITY_UNIT);
            ebeanAlert2.setCluster(TEST_CLUSTER);
            ebeanAlert2.setUuid(UUID.randomUUID());
            ebeanAlert2.setMetric(TEST_METRIC);
            ebeanAlert2.setContext(TEST_CONTEXT);
            ebeanAlert2.setService(TEST_SERVICE);
            Ebean.save(ebeanAlert2);
            transaction.commit();
        } finally {
            transaction.end();
        }
        Assert.assertEquals(2, alertRepo.getAlertCount());
    }

    @Test
    public void addOrUpdateAlertAddCase() {
        final UUID uuid = UUID.randomUUID();
        Assert.assertFalse(alertRepo.get(uuid).isPresent());
        final Alert actualAlert = alertBuilder.setId(uuid).build();
        alertRepo.addOrUpdateAlert(actualAlert);
        final Optional<Alert> expected = alertRepo.get(uuid);
        Assert.assertTrue(expected.isPresent());
        Assert.assertEquals(uuid, expected.get().getId());
        Assert.assertEquals(TEST_CLUSTER, expected.get().getCluster());
        Assert.assertEquals(TEST_METRIC, expected.get().getMetric());
        Assert.assertEquals(TEST_EXTENSIONS, expected.get().getExtensions());
        Assert.assertEquals(TEST_SERVICE, expected.get().getService());
        Assert.assertEquals(TEST_NAME, expected.get().getName());
        Assert.assertEquals(TEST_OPERATOR, expected.get().getOperator());
        Assert.assertEquals(Period.seconds(TEST_PERIOD_IN_SECONDS), expected.get().getPeriod());
        Assert.assertEquals(TEST_STATISTIC, expected.get().getStatistic());
        Assert.assertEquals(TEST_QUANTITY, expected.get().getValue());
        Assert.assertEquals(TEST_CONTEXT, expected.get().getContext());
    }

    @Test
    public void addOrUpdateAlertUpdateCase() throws JsonProcessingException {
        final UUID uuid = UUID.randomUUID();
        final Transaction transaction = Ebean.beginTransaction();
        try {
            final models.ebean.Alert ebeanAlert = new models.ebean.Alert();
            ebeanAlert.setNagiosExtension(TEST_NAGIOS_EXTENSIONS);
            ebeanAlert.setName(TEST_NAME);
            ebeanAlert.setOperator(TEST_OPERATOR);
            ebeanAlert.setPeriod(TEST_PERIOD_IN_SECONDS);
            ebeanAlert.setStatistic(TEST_STATISTIC);
            ebeanAlert.setQuantityValue(TEST_QUANTITY_VALUE);
            ebeanAlert.setQuantityUnit(TEST_QUANTITY_UNIT);
            ebeanAlert.setCluster(TEST_CLUSTER);
            ebeanAlert.setUuid(uuid);
            ebeanAlert.setMetric(TEST_METRIC);
            ebeanAlert.setContext(TEST_CONTEXT);
            ebeanAlert.setService(TEST_SERVICE);
            Ebean.save(ebeanAlert);
            transaction.commit();
        } finally {
            transaction.end();
        }
        alertRepo.addOrUpdateAlert(alertBuilder
                .setId(uuid)
                .setCluster("new-cluster")
                .build());
        final Optional<Alert> expected = alertRepo.get(uuid);
        Assert.assertTrue(expected.isPresent());
        Assert.assertEquals(uuid, expected.get().getId());
        Assert.assertEquals("new-cluster", expected.get().getCluster());
        Assert.assertEquals(TEST_METRIC, expected.get().getMetric());
        Assert.assertEquals(TEST_EXTENSIONS, expected.get().getExtensions());
        Assert.assertEquals(TEST_SERVICE, expected.get().getService());
        Assert.assertEquals(TEST_NAME, expected.get().getName());
        Assert.assertEquals(TEST_OPERATOR, expected.get().getOperator());
        Assert.assertEquals(Period.seconds(TEST_PERIOD_IN_SECONDS), expected.get().getPeriod());
        Assert.assertEquals(TEST_STATISTIC, expected.get().getStatistic());
        Assert.assertEquals(TEST_QUANTITY, expected.get().getValue());
        Assert.assertEquals(TEST_CONTEXT, expected.get().getContext());
    }

    @Test
    public void addAlertWithNoExtension() {
        final UUID uuid = UUID.randomUUID();
        final Alert alert = alertBuilder
                .setId(uuid)
                .setExtensions(Collections.emptyMap())
                .build();
        alertRepo.addOrUpdateAlert(alert);
        final Alert expectedAlert = alertRepo.get(uuid).get();
        Assert.assertTrue(expectedAlert.getExtensions().isEmpty());
    }

    private final DatabaseAlertRepository.AlertQueryGenerator queryGenerator = new DatabaseAlertRepository.GenericQueryGenerator();
    private final DatabaseAlertRepository alertRepo = new DatabaseAlertRepository(queryGenerator);
    private final DefaultAlert.Builder alertBuilder = new DefaultAlert.Builder()
            .setId(UUID.randomUUID())
            .setCluster(TEST_CLUSTER)
            .setMetric(TEST_METRIC)
            .setContext(TEST_CONTEXT)
            .setService(TEST_SERVICE)
            .setExtensions(TEST_EXTENSIONS)
            .setName(TEST_NAME)
            .setOperator(TEST_OPERATOR)
            .setPeriod(Period.seconds(TEST_PERIOD_IN_SECONDS))
            .setStatistic(TEST_STATISTIC)
            .setValue(TEST_QUANTITY);

    private static final String TEST_CLUSTER = "test-cluster";
    private static final String TEST_METRIC = "test-metric";
    private static final String TEST_SERVICE = "test-service";
    private static final Context TEST_CONTEXT = Context.CLUSTER;
    private static final Map<String, Object> TEST_EXTENSIONS = ImmutableMap.of("severity", "CRITICAL", "notify", "abc@groupon.com", "maxCheckAttempts", 3, "freshnessThreshold", 5);
    private static final String TEST_NAME = "test-name";
    private static final Operator TEST_OPERATOR = Operator.EQUAL_TO;
    private static final int TEST_PERIOD_IN_SECONDS = 600;
    private static final String TEST_STATISTIC = "test-statistic";
    private static final double TEST_QUANTITY_VALUE = 12.0;
    private static final String TEST_QUANTITY_UNIT = "test-unit";
    private static final Quantity TEST_QUANTITY = new DefaultQuantity.Builder()
            .setValue(TEST_QUANTITY_VALUE)
            .setUnit(TEST_QUANTITY_UNIT)
            .build();
    private static final NagiosExtension TEST_NAGIOS_EXTENSIONS = new NagiosExtension();

    static {
        TEST_NAGIOS_EXTENSIONS.setSeverity("CRITICAL");
        TEST_NAGIOS_EXTENSIONS.setNotify("abc@groupon.com");
        TEST_NAGIOS_EXTENSIONS.setMaxCheckAttempts(3);
        TEST_NAGIOS_EXTENSIONS.setFreshnessThresholdInSeconds(5);
    }
}
