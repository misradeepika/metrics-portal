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

import com.arpnetworking.metrics.portal.TestBeanFactory;
import com.arpnetworking.metrics.portal.alerts.AlertRepository;
import com.arpnetworking.metrics.portal.alerts.impl.DatabaseAlertRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.internal.Alert;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import play.Application;
import play.Configuration;
import play.inject.Bindings;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;

import java.io.IOException;
import java.util.UUID;

/**
 * Tests class <code>AlertController</code>.
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
    public void testCreateValidCase() throws IOException {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("PUT")
                .bodyJson(readTree("testCreateValidCase"))
                .header("Content-Type", "application/json")
                .uri("/v1/alerts");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.OK, result.status());
    }

    @Test
    public void testCreateMissingBodyCase() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("PUT")
                .header("Content-Type", "application/json")
                .uri("/v1/alerts");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    public void testCreateMissingIdCase() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("PUT")
                .bodyJson(readTree("testCreateMissingIdCase"))
                .header("Content-Type", "application/json")
                .uri("/v1/alerts");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    public void testCreateMissingContextCase() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("PUT")
                .bodyJson(readTree("testCreateMissingContextCase"))
                .header("Content-Type", "application/json")
                .uri("/v1/alerts");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    public void testCreateInvalidContextCase() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("PUT")
                .bodyJson(readTree("testCreateInvalidContextCase"))
                .header("Content-Type", "application/json")
                .uri("/v1/alerts");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    public void testCreateMissingNameCase() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("PUT")
                .bodyJson(readTree("testCreateMissingNameCase"))
                .header("Content-Type", "application/json")
                .uri("/v1/alerts");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    public void testCreateMissingClusterCase() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("PUT")
                .bodyJson(readTree("testCreateMissingClusterCase"))
                .header("Content-Type", "application/json")
                .uri("/v1/alerts");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    public void testCreateMissingMetricCase() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("PUT")
                .bodyJson(readTree("testCreateMissingMetricCase"))
                .header("Content-Type", "application/json")
                .uri("/v1/alerts");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    public void testCreateMissingStatisticCase() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("PUT")
                .bodyJson(readTree("testCreateMissingStatisticCase"))
                .header("Content-Type", "application/json")
                .uri("/v1/alerts");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    public void testCreateMissingServiceCase() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("PUT")
                .bodyJson(readTree("testCreateMissingServiceCase"))
                .header("Content-Type", "application/json")
                .uri("/v1/alerts");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    public void testCreateMissingPeriodCase() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("PUT")
                .bodyJson(readTree("testCreateMissingPeriodCase"))
                .header("Content-Type", "application/json")
                .uri("/v1/alerts");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    public void testCreateInvalidPeriodCase() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("PUT")
                .bodyJson(readTree("testCreateInvalidPeriodCase"))
                .header("Content-Type", "application/json")
                .uri("/v1/alerts");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    public void testCreateMissingOperatorCase() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("PUT")
                .bodyJson(readTree("testCreateMissingOperatorCase"))
                .header("Content-Type", "application/json")
                .uri("/v1/alerts");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    public void testCreateInvalidOperatorCase() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("PUT")
                .bodyJson(readTree("testCreateInvalidOperatorCase"))
                .header("Content-Type", "application/json")
                .uri("/v1/alerts");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    public void testCreateMissingValueCase() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("PUT")
                .bodyJson(readTree("testCreateMissingValueCase"))
                .header("Content-Type", "application/json")
                .uri("/v1/alerts");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    public void testCreateMissingExtensionsCase() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("PUT")
                .bodyJson(readTree("testCreateMissingExtensionsCase"))
                .header("Content-Type", "application/json")
                .uri("/v1/alerts");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.OK, result.status());
    }

    @Test
    public void testCreateEmptyExtensionsCase() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("PUT")
                .bodyJson(readTree("testCreateEmptyExtensionsCase"))
                .header("Content-Type", "application/json")
                .uri("/v1/alerts");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.OK, result.status());
    }

    @Test
    public void testUpdateValidCase() throws IOException {
        final UUID uuid = UUID.fromString("e62368dc-1421-11e3-91c1-00259069c2f0");
        Alert originalAlert = TestBeanFactory.createAlertBuilder().setId(uuid).build();
        alertRepo.addOrUpdateAlert(originalAlert);
        final JsonNode body = readTree("testUpdateValidCase");
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("PUT")
                .bodyJson(body)
                .header("Content-Type", "application/json")
                .uri("/v1/alerts");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.OK, result.status());
    }

    private JsonNode readTree(final String resourceSuffix) {
        try {
            return OBJECT_MAPPER.readTree(getClass().getClassLoader().getResource("controllers/" + CLASS_NAME + "." + resourceSuffix + ".json"));
        } catch (final IOException e) {
            Assert.fail("Failed with exception: " + e);
            return null;
        }
    }

    private static Application app;
    private static final AlertRepository alertRepo = new DatabaseAlertRepository(new DatabaseAlertRepository.GenericQueryGenerator());
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String CLASS_NAME = AlertControllerTest.class.getSimpleName();
}
