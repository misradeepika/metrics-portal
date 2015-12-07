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
package controllers;

import com.arpnetworking.metrics.portal.alerts.AlertRepository;
import com.arpnetworking.metrics.portal.alerts.impl.DatabaseAlertRepository;
import models.internal.Alert;
import models.internal.Context;
import models.internal.Operator;
import models.internal.impl.DefaultAlert;
import models.internal.impl.DefaultQuantity;
import org.joda.time.Period;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import play.Application;
import play.Configuration;
import play.inject.Bindings;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;

import java.util.UUID;

/**
 * Tests class <code>AlertComtroller</code>.
 *
 * @author Deepika Misra (deepika at groupon dot com)
 */
public class AlertControllerTest {

    @BeforeClass
    public static void instantiate() {
        Configuration configuration = Configuration.empty();
        app = new GuiceApplicationBuilder()
                .bindings(Bindings.bind(AlertController.class).toInstance(new AlertController(configuration, alertRepo)))
                .build();
        Helpers.start(app);
        alertRepo.open();
    }

    @AfterClass
    public static void shutdown() {
        alertRepo.close();
        if (app != null) {
            Helpers.stop(app);
        }
    }

    @Test
    public void testCreateValidCase() {
        final String body = "{\"context\":\"CLUSTER\", \"name\":\"test-name\", \"cluster\":\"test-cluster\", "
                + "\"metric\":\"test-metric\", \"service\":\"test-service\", \"statistic\": \"test-statistic\", "
                + "\"period\": 1000, \"operator\":\"EQUAL_TO\", \"value\":{\"value\":12.0, \"unit\":\"MEGABYTE\"}, "
                + "\"extensions\":{}}";
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("POST")
                .bodyJson(Json.parse(body))
                .header("Content-Type", "application/json")
                .uri("/v1/alerts");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.OK, result.status());
    }

    @Test
    public void testCreateMissingBodyCase() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("POST")
                .header("Content-Type", "application/json")
                .uri("/v1/alerts");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    public void testCreateMissingContextCase() {
        final String body = "{\"name\":\"test-name\", \"cluster\":\"test-cluster\", "
                + "\"metric\":\"test-metric\", \"service\":\"test-service\", \"statistic\": \"test-statistic\", "
                + "\"period\": 1000, \"operator\":\"EQUAL_TO\", \"value\":{\"value\":12.0, \"unit\":\"MEGABYTE\"}, "
                + "\"extensions\":{}}";
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("POST")
                .bodyJson(Json.parse(body))
                .header("Content-Type", "application/json")
                .uri("/v1/alerts");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    public void testCreateInvalidContextCase() {
        final String body = "{\"context\":\"invalid\", \"name\":\"test-name\", \"cluster\":\"test-cluster\", "
                + "\"metric\":\"test-metric\", \"service\":\"test-service\", \"statistic\": \"test-statistic\", "
                + "\"period\": 1000, \"operator\":\"EQUAL_TO\", \"value\":{\"value\":12.0, \"unit\":\"MEGABYTE\"}, "
                + "\"extensions\":{}}";
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("POST")
                .bodyJson(Json.parse(body))
                .header("Content-Type", "application/json")
                .uri("/v1/alerts");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    public void testCreateMissingNameCase() {
        final String body = "{\"context\":\"CLUSTER\", \"cluster\":\"test-cluster\", "
                + "\"metric\":\"test-metric\", \"service\":\"test-service\", \"statistic\": \"test-statistic\", "
                + "\"period\": 1000, \"operator\":\"EQUAL_TO\", \"value\":{\"value\":12.0, \"unit\":\"MEGABYTE\"}, "
                + "\"extensions\":{}}";
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("POST")
                .bodyJson(Json.parse(body))
                .header("Content-Type", "application/json")
                .uri("/v1/alerts");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    public void testCreateMissingClusterCase() {
        final String body = "{\"context\":\"CLUSTER\", \"name\":\"test-name\", "
                + "\"metric\":\"test-metric\", \"service\":\"test-service\", \"statistic\": \"test-statistic\", "
                + "\"period\": 1000, \"operator\":\"EQUAL_TO\", \"value\":{\"value\":12.0, \"unit\":\"MEGABYTE\"}, "
                + "\"extensions\":{}}";
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("POST")
                .bodyJson(Json.parse(body))
                .header("Content-Type", "application/json")
                .uri("/v1/alerts");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    public void testCreateMissingMetricCase() {
        final String body = "{\"context\":\"CLUSTER\", \"name\":\"test-name\", \"cluster\":\"test-cluster\", "
                + "\"service\":\"test-service\", \"statistic\": \"test-statistic\", "
                + "\"period\": 1000, \"operator\":\"EQUAL_TO\", \"value\":{\"value\":12.0, \"unit\":\"MEGABYTE\"}, "
                + "\"extensions\":{}}";
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("POST")
                .bodyJson(Json.parse(body))
                .header("Content-Type", "application/json")
                .uri("/v1/alerts");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    public void testCreateMissingStatisticCase() {
        final String body = "{\"context\":\"CLUSTER\", \"name\":\"test-name\", \"cluster\":\"test-cluster\", "
                + "\"metric\":\"test-metric\", \"service\":\"test-service\", "
                + "\"period\": 1000, \"operator\":\"EQUAL_TO\", \"value\":{\"value\":12.0, \"unit\":\"MEGABYTE\"}, "
                + "\"extensions\":{}}";
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("POST")
                .bodyJson(Json.parse(body))
                .header("Content-Type", "application/json")
                .uri("/v1/alerts");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    public void testCreateMissingServiceCase() {
        final String body = "{\"context\":\"CLUSTER\", \"name\":\"test-name\", \"cluster\":\"test-cluster\", "
                + "\"metric\":\"test-metric\", \"statistic\": \"test-statistic\", "
                + "\"period\": 1000, \"operator\":\"EQUAL_TO\", \"value\":{\"value\":12.0, \"unit\":\"MEGABYTE\"}, "
                + "\"extensions\":{}}";
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("POST")
                .bodyJson(Json.parse(body))
                .header("Content-Type", "application/json")
                .uri("/v1/alerts");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    public void testCreateMissingPeriodCase() {
        final String body = "{\"context\":\"CLUSTER\", \"name\":\"test-name\", \"cluster\":\"test-cluster\", "
                + "\"metric\":\"test-metric\", \"statistic\": \"test-statistic\", "
                + "\"operator\":\"EQUAL_TO\", \"value\":{\"value\":12.0, \"unit\":\"MEGABYTE\"}, "
                + "\"extensions\":{}}";
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("POST")
                .bodyJson(Json.parse(body))
                .header("Content-Type", "application/json")
                .uri("/v1/alerts");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    public void testCreateInvalidPeriodCase() {
        final String body = "{\"context\":\"CLUSTER\", \"name\":\"test-name\", \"cluster\":\"test-cluster\", "
                + "\"metric\":\"test-metric\", \"statistic\": \"test-statistic\", "
                + "\"period\":\"invalid\", \"operator\":\"EQUAL_TO\", \"value\":{\"value\":12.0, \"unit\":\"MEGABYTE\"}, "
                + "\"extensions\":{}}";
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("POST")
                .bodyJson(Json.parse(body))
                .header("Content-Type", "application/json")
                .uri("/v1/alerts");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    public void testCreateMissingOperatorCase() {
        final String body = "{\"context\":\"CLUSTER\", \"name\":\"test-name\", \"cluster\":\"test-cluster\", "
                + "\"metric\":\"test-metric\", \"statistic\": \"test-statistic\", "
                + "\"period\": 1000, \"value\":{\"value\":12.0, \"unit\":\"MEGABYTE\"}, "
                + "\"extensions\":{}}";
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("POST")
                .bodyJson(Json.parse(body))
                .header("Content-Type", "application/json")
                .uri("/v1/alerts");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    public void testCreateInvalidOperatorCase() {
        final String body = "{\"context\":\"CLUSTER\", \"name\":\"test-name\", \"cluster\":\"test-cluster\", "
                + "\"metric\":\"test-metric\", \"statistic\": \"test-statistic\", "
                + "\"period\": 1000,  \"operator\":\"invalid\", \"value\":{\"value\":12.0, \"unit\":\"MEGABYTE\"}, "
                + "\"extensions\":{}}";
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("POST")
                .bodyJson(Json.parse(body))
                .header("Content-Type", "application/json")
                .uri("/v1/alerts");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    public void testCreateMissingValueCase() {
        final String body = "{\"context\":\"CLUSTER\", \"name\":\"test-name\", \"cluster\":\"test-cluster\", "
                + "\"metric\":\"test-metric\", \"statistic\": \"test-statistic\", "
                + "\"period\": 1000, \"operator\":\"EQUAL_TO\", \"extensions\":{}}";
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("POST")
                .bodyJson(Json.parse(body))
                .header("Content-Type", "application/json")
                .uri("/v1/alerts");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    public void testCreateInvalidValueCase() {
        final String body = "{\"context\":\"CLUSTER\", \"name\":\"test-name\", \"cluster\":\"test-cluster\", "
                + "\"metric\":\"test-metric\", \"statistic\": \"test-statistic\", "
                + "\"period\": 1000, \"operator\":\"EQUAL_TO\", \"value\":\"invalid\", \"extensions\":{}}";
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("POST")
                .bodyJson(Json.parse(body))
                .header("Content-Type", "application/json")
                .uri("/v1/alerts");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    public void testCreateMissingExtensionsCase() {
        final String body = "{\"context\":\"CLUSTER\", \"name\":\"test-name\", \"cluster\":\"test-cluster\", "
                + "\"metric\":\"test-metric\", \"service\":\"test-service\", \"statistic\": \"test-statistic\", "
                + "\"period\": 1000, \"operator\":\"EQUAL_TO\", \"value\":{\"value\":12.0, \"unit\":\"MEGABYTE\"}}";
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("POST")
                .bodyJson(Json.parse(body))
                .header("Content-Type", "application/json")
                .uri("/v1/alerts");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.OK, result.status());
    }

    @Test
    public void testUpdateValidCase() {
        Alert originalAlert = new DefaultAlert.Builder()
                .setId(UUID.randomUUID())
                .setCluster("original-cluster")
                .setContext(Context.HOST)
                .setMetric("original-metric")
                .setName("original-name")
                .setOperator(Operator.NOT_EQUAL_TO)
                .setPeriod(Period.minutes(5))
                .setService("original-service")
                .setStatistic("original-statistic")
                .setValue(new DefaultQuantity.Builder().setValue(20.0).setUnit("KILOBYTE").build())
                .build();
        alertRepo.addOrUpdateAlert(originalAlert);
        final String body = "{\"context\":\"CLUSTER\", \"name\":\"test-name\", \"cluster\":\"test-cluster\", "
                + "\"metric\":\"test-metric\", \"service\":\"test-service\", \"statistic\": \"test-statistic\", "
                + "\"period\": 1000, \"operator\":\"EQUAL_TO\", \"value\":{\"value\":12.0, \"unit\":\"MEGABYTE\"}}";
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("PUT")
                .bodyJson(Json.parse(body))
                .header("Content-Type", "application/json")
                .uri("/v1/alerts/" + originalAlert.getId().toString());
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.OK, result.status());
        Alert expectedAlert = alertRepo.get(originalAlert.getId()).get();
        Assert.assertEquals(Context.CLUSTER, expectedAlert.getContext());
        Assert.assertEquals("test-cluster", expectedAlert.getCluster());
        Assert.assertEquals("test-metric", expectedAlert.getMetric());
        Assert.assertEquals("test-name", expectedAlert.getName());
        Assert.assertEquals(Operator.EQUAL_TO, expectedAlert.getOperator());
        Assert.assertEquals(Period.seconds(1), expectedAlert.getPeriod());
        Assert.assertEquals("test-service", expectedAlert.getService());
        Assert.assertEquals("test-statistic", expectedAlert.getStatistic());
        Assert.assertEquals(12.0, expectedAlert.getValue().getValue(), 0);
        Assert.assertTrue(expectedAlert.getValue().getUnit().isPresent());
    }

    @Test
    public void testUpdateInvalidAlertId() {
        final String body = "{\"context\":\"CLUSTER\", \"name\":\"test-name\", \"cluster\":\"test-cluster\", "
                + "\"metric\":\"test-metric\", \"service\":\"test-service\", \"statistic\": \"test-statistic\", "
                + "\"period\": 1000, \"operator\":\"EQUAL_TO\", \"value\":{\"value\":12.0, \"unit\":\"MEGABYTE\"}}";
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("PUT")
                .bodyJson(Json.parse(body))
                .header("Content-Type", "application/json")
                .uri("/v1/alerts/invalidUuid");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    public void testUpdateAlertIdNotExist() {
        final UUID alertId = UUID.randomUUID();
        Assert.assertFalse(alertRepo.get(alertId).isPresent());
        final String body = "{\"context\":\"CLUSTER\", \"name\":\"test-name\", \"cluster\":\"test-cluster\", "
                + "\"metric\":\"test-metric\", \"service\":\"test-service\", \"statistic\": \"test-statistic\", "
                + "\"period\": 1000, \"operator\":\"EQUAL_TO\", \"value\":{\"value\":12.0, \"unit\":\"MEGABYTE\"}}";
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("PUT")
                .bodyJson(Json.parse(body))
                .header("Content-Type", "application/json")
                .uri("/v1/alerts/" + alertId.toString());
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.OK, result.status());
        Assert.assertTrue(alertRepo.get(alertId).isPresent());
        Alert expectedAlert = alertRepo.get(alertId).get();
        Assert.assertEquals(Context.CLUSTER, expectedAlert.getContext());
        Assert.assertEquals("test-cluster", expectedAlert.getCluster());
        Assert.assertEquals("test-metric", expectedAlert.getMetric());
        Assert.assertEquals("test-name", expectedAlert.getName());
        Assert.assertEquals(Operator.EQUAL_TO, expectedAlert.getOperator());
        Assert.assertEquals(Period.seconds(1), expectedAlert.getPeriod());
        Assert.assertEquals("test-service", expectedAlert.getService());
        Assert.assertEquals("test-statistic", expectedAlert.getStatistic());
        Assert.assertEquals(12.0, expectedAlert.getValue().getValue(), 0);
        Assert.assertTrue(expectedAlert.getValue().getUnit().isPresent());
    }

    public static Application app;
    public static final AlertRepository alertRepo = new DatabaseAlertRepository(new DatabaseAlertRepository.GenericQueryGenerator());
}
