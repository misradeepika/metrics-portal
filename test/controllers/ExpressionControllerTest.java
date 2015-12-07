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

import com.arpnetworking.metrics.portal.expressions.ExpressionRepository;
import com.arpnetworking.metrics.portal.expressions.impl.DatabaseExpressionRepository;
import models.internal.Expression;
import models.internal.impl.DefaultExpression;
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
 * Tests <code>ExpressionController</code>.
 *
 * @author Deepika Misra (deepika at groupon dot com)
 */
public class ExpressionControllerTest {

    @BeforeClass
    public static void instantiate() {
        Configuration configuration = Configuration.empty();
        app = new GuiceApplicationBuilder()
                .bindings(Bindings.bind(ExpressionController.class).toInstance(new ExpressionController(configuration, exprRepo)))
                .build();
        Helpers.start(app);
        exprRepo.open();
    }

    @AfterClass
    public static void shutdown() {
        exprRepo.close();
        if (app != null) {
            Helpers.stop(app);
        }
    }

    @Test
    public void testCreateValidCase() {
        final String body = "{\"cluster\":\"test-cluster\", \"metric\":\"test-metric\", \"script\":\"test-script\", \"service\":\"test-service\"}";
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("POST")
                .bodyJson(Json.parse(body))
                .header("Content-Type", "application/json")
                .uri("/v1/expressions");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.OK, result.status());
    }

    @Test
    public void testCreateMissingBodyCase() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("POST")
                .header("Content-Type", "application/json")
                .uri("/v1/expressions");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    public void testCreateMissingClusterCase() {
        final String body = "{\"metric\":\"test-metric\", \"script\":\"test-script\", \"service\":\"test-service\"}";
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("POST")
                .bodyJson(Json.parse(body))
                .header("Content-Type", "application/json")
                .uri("/v1/expressions");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    public void testCreateMissingMetricCase() {
        final String body = "{\"cluster\":\"test-cluster\", \"script\":\"test-script\", \"service\":\"test-service\"}";
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("POST")
                .bodyJson(Json.parse(body))
                .header("Content-Type", "application/json")
                .uri("/v1/expressions");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    public void testCreateMissingScriptCase() {
        final String body = "{\"cluster\":\"test-cluster\", \"metric\":\"test-metric\", \"service\":\"test-service\"}";
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("POST")
                .bodyJson(Json.parse(body))
                .header("Content-Type", "application/json")
                .uri("/v1/expressions");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    public void testCreateMissingServiceCase() {
        final String body = "{\"cluster\":\"test-cluster\", \"metric\":\"test-metric\", \"script\":\"test-script\"}";
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("POST")
                .bodyJson(Json.parse(body))
                .header("Content-Type", "application/json")
                .uri("/v1/expressions");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    public void testUpdateValidCase() {
        Expression originalExpr = new DefaultExpression.Builder()
                .setCluster("original-expr")
                .setId(UUID.randomUUID())
                .setMetric("original-metric")
                .setScript("original-script")
                .setService("original-service")
                .build();
        exprRepo.addOrUpdateExpression(originalExpr);
        final String body = "{\"cluster\":\"test-cluster\", \"metric\":\"test-metric\", \"script\":\"test-script\", \"service\":\"test-service\"}";
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("PUT")
                .bodyJson(Json.parse(body))
                .header("Content-Type", "application/json")
                .uri("/v1/expressions/" + originalExpr.getId().toString());
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.OK, result.status());
        Expression expectedExpr = exprRepo.get(originalExpr.getId()).get();
        Assert.assertEquals("test-cluster", expectedExpr.getCluster());
        Assert.assertEquals("test-metric", expectedExpr.getMetric());
        Assert.assertEquals("test-script", expectedExpr.getScript());
        Assert.assertEquals("test-service", expectedExpr.getService());
    }

    @Test
    public void testUpdateInvalidExprId() {
        final String body = "{\"cluster\":\"test-cluster\", \"metric\":\"test-metric\", \"script\":\"test-script\", \"service\":\"test-service\"}";
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("PUT")
                .bodyJson(Json.parse(body))
                .header("Content-Type", "application/json")
                .uri("/v1/expressions/invalidUuid");
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    public void testUpdateExprIdNotExist() {
        final UUID exprId = UUID.randomUUID();
        Assert.assertFalse(exprRepo.get(exprId).isPresent());
        final String body = "{\"cluster\":\"test-cluster\", \"metric\":\"test-metric\", \"script\":\"test-script\", \"service\":\"test-service\"}";
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method("PUT")
                .bodyJson(Json.parse(body))
                .header("Content-Type", "application/json")
                .uri("/v1/expressions/" + exprId.toString());
        Result result = Helpers.route(request);
        Assert.assertEquals(Http.Status.OK, result.status());
        Assert.assertTrue(exprRepo.get(exprId).isPresent());
        Expression expectedExpr = exprRepo.get(exprId).get();
        Assert.assertEquals("test-cluster", expectedExpr.getCluster());
        Assert.assertEquals("test-metric", expectedExpr.getMetric());
        Assert.assertEquals("test-script", expectedExpr.getScript());
        Assert.assertEquals("test-service", expectedExpr.getService());
    }

    public static Application app;
    public static final ExpressionRepository exprRepo = new DatabaseExpressionRepository(new DatabaseExpressionRepository.GenericQueryGenerator());
}
