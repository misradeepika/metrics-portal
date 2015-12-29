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

import com.arpnetworking.metrics.portal.alerts.AlertRepository;
import com.arpnetworking.play.configuration.ConfigurationHelper;
import com.arpnetworking.steno.Logger;
import com.arpnetworking.steno.LoggerFactory;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.PagedList;
import com.avaje.ebean.Query;
import com.avaje.ebean.Transaction;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import models.ebean.NagiosExtension;
import models.internal.Alert;
import models.internal.AlertQuery;
import models.internal.QueryResult;
import models.internal.impl.DefaultAlert;
import models.internal.impl.DefaultAlertQuery;
import models.internal.impl.DefaultQuantity;
import models.internal.impl.DefaultQueryResult;
import org.joda.time.Period;
import play.Configuration;
import play.Environment;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implementation of <code>AlertRepository</code> using SQL database.
 *
 * @author Deepika Misra (deepika at groupon dot com)
 */
public class DatabaseAlertRepository implements AlertRepository {

    /**
     * Public constructor.
     *
     * @param environment Play's <code>Environment</code> instance.
     * @param config      Play's <code>Configuration</code> instance.
     * @throws Exception If the configuration is invalid.
     */
    @Inject
    public DatabaseAlertRepository(final Environment environment, final Configuration config) throws Exception {
        this(
                ConfigurationHelper.<AlertQueryGenerator>getType(
                        environment,
                        config,
                        "alertRepository.alertQueryGenerator.type")
                        .newInstance());
    }

    /**
     * Public constructor.
     *
     * @param alertQueryGenerator Instance of <code>AlertQueryGenerator</code>.
     */
    public DatabaseAlertRepository(final AlertQueryGenerator alertQueryGenerator) {
        _alertQueryGenerator = alertQueryGenerator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void open() {
        assertIsOpen(false);
        LOGGER.debug().setMessage("Opening alert repository").log();
        _isOpen.set(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        assertIsOpen();
        LOGGER.debug().setMessage("Closing alert repository").log();
        _isOpen.set(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Alert> get(final UUID identifier) {
        assertIsOpen();
        LOGGER.debug()
                .setMessage("Getting alert")
                .addData("alertId", identifier)
                .log();

        final models.ebean.Alert ebeanAlert = Ebean.find(models.ebean.Alert.class)
                .where()
                .eq("uuid", identifier)
                .findUnique();
        if (ebeanAlert == null) {
            return Optional.empty();
        }
        final Alert result = new DefaultAlert.Builder()
                .setId(ebeanAlert.getUuid())
                .setName(ebeanAlert.getName())
                .setContext(ebeanAlert.getContext())
                .setCluster(ebeanAlert.getCluster())
                .setService(ebeanAlert.getService())
                .setMetric(ebeanAlert.getMetric())
                .setStatistic(ebeanAlert.getStatistic())
                .setPeriod(Period.seconds(ebeanAlert.getPeriod()))
                .setOperator(ebeanAlert.getOperator())
                .setValue(new DefaultQuantity.Builder()
                        .setValue(ebeanAlert.getQuantityValue())
                        .setUnit(ebeanAlert.getQuantityUnit())
                        .build())
                .setExtensions(mergeExtensions(ebeanAlert.getNagiosExtension()))
                .build();
        return Optional.of(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AlertQuery createQuery() {
        assertIsOpen();
        LOGGER.debug().setMessage("Preparing query").log();
        return new DefaultAlertQuery(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueryResult<Alert> query(final AlertQuery query) {
        assertIsOpen();
        LOGGER.debug()
                .setMessage("Querying")
                .addData("query", query)
                .log();

        // Create the base query
        final PagedList<models.ebean.Alert> pagedAlerts = _alertQueryGenerator.createAlertQuery(query);

        // Compute the etag
        // NOTE: Another way to do this would be to use the version field and hash those together.
        final String etag = Long.toHexString(pagedAlerts.getList().stream()
                .map(alert -> alert.getUpdatedAt().after(alert.getCreatedAt()) ? alert.getUpdatedAt() : alert.getCreatedAt())
                .max(Timestamp::compareTo)
                .orElse(new Timestamp(0))
                .getTime());

        final List<Alert> values = new ArrayList<>();
        pagedAlerts.getList().forEach(ebeanAlert -> {
            final Alert result = new DefaultAlert.Builder()
                    .setCluster(ebeanAlert.getCluster())
                    .setContext(ebeanAlert.getContext())
                    .setId(ebeanAlert.getUuid())
                    .setMetric(ebeanAlert.getMetric())
                    .setName(ebeanAlert.getName())
                    .setOperator(ebeanAlert.getOperator())
                    .setPeriod(Period.seconds(ebeanAlert.getPeriod()))
                    .setService(ebeanAlert.getService())
                    .setStatistic(ebeanAlert.getStatistic())
                    .setValue(new DefaultQuantity.Builder()
                            .setValue(ebeanAlert.getQuantityValue())
                            .setUnit(ebeanAlert.getQuantityUnit())
                            .build())
                    .setExtensions(mergeExtensions(ebeanAlert.getNagiosExtension()))
                    .build();
            values.add(result);
        });

        // Transform the results
        return new DefaultQueryResult<>(values, pagedAlerts.getTotalRowCount(), etag);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getAlertCount() {
        assertIsOpen();
        return Ebean.find(models.ebean.Alert.class)
                .findRowCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addOrUpdateAlert(final Alert alert) {
        assertIsOpen();
        LOGGER.debug()
                .setMessage("Upserting alert")
                .addData("alert", alert)
                .log();

        final Transaction transaction = Ebean.beginTransaction();

        try {
            models.ebean.Alert ebeanAlert = Ebean.find(models.ebean.Alert.class)
                    .where()
                    .eq("uuid", alert.getId())
                    .findUnique();
            boolean created = false;
            if (ebeanAlert == null) {
                ebeanAlert = new models.ebean.Alert();
                created = true;
            }

            ebeanAlert.setCluster(alert.getCluster());
            ebeanAlert.setUuid(alert.getId());
            ebeanAlert.setMetric(alert.getMetric());
            ebeanAlert.setContext(alert.getContext());
            ebeanAlert.setNagiosExtension(getNagiosExtensionFromMap(alert.getExtensions()));
            ebeanAlert.setName(alert.getName());
            ebeanAlert.setOperator(alert.getOperator());
            ebeanAlert.setPeriod(alert.getPeriod().toStandardSeconds().getSeconds());
            ebeanAlert.setQuantityValue(alert.getValue().getValue());
            ebeanAlert.setQuantityUnit(alert.getValue().getUnit().isPresent() ? alert.getValue().getUnit().get() : null);
            ebeanAlert.setStatistic(alert.getStatistic());
            ebeanAlert.setService(alert.getService());
            _alertQueryGenerator.saveAlert(ebeanAlert);
            transaction.commit();

            LOGGER.info()
                    .setMessage("Upserted alert")
                    .addData("alert", alert)
                    .addData("isCreated", created)
                    .log();
        } finally {
            transaction.end();
        }
    }

    //NOTE: Only to be used to clear the tables for testing
    /**
     * Package private.
     */
    void deleteAll() {
        assertIsOpen();
        LOGGER.debug()
                .setMessage("Deleting all alerts")
                .log();
        final List<models.ebean.Alert> alerts = Ebean.find(models.ebean.Alert.class).findList();
        final Transaction transaction = Ebean.beginTransaction();
        try {
            Ebean.delete(alerts);
            transaction.commit();
        } finally {
            transaction.end();
        }
    }

    private void assertIsOpen() {
        assertIsOpen(true);
    }

    private void assertIsOpen(final boolean expectedState) {
        if (_isOpen.get() != expectedState) {
            throw new IllegalStateException(String.format("Alert repository is not %s", expectedState ? "open" : "closed"));
        }
    }

    private NagiosExtension getNagiosExtensionFromMap(final Map<String, Object> extensionsMap) {
        if (extensionsMap == null || extensionsMap.isEmpty()) {
            return null;
        }
        final NagiosExtension nagiosExtension = new NagiosExtension();
        nagiosExtension.setSeverity((String) extensionsMap.get("severity"));
        nagiosExtension.setNotify((String) extensionsMap.get("notify"));
        nagiosExtension.setMaxCheckAttempts((Integer) extensionsMap.get("maxCheckAttempts"));
        nagiosExtension.setFreshnessThresholdInSeconds((Integer) extensionsMap.get("freshnessThreshold"));
        return nagiosExtension;
    }

    private Map<String, Object> mergeExtensions(final NagiosExtension nagiosExtension) {
        final Map<String, Object> extensionMap = Maps.newHashMap();
        if (nagiosExtension == null) {
            return Collections.emptyMap();
        }
        extensionMap.put(NAGIOS_EXTENSION_SEVERITY_KEY, nagiosExtension.getSeverity());
        extensionMap.put(NAGIOS_EXTENSION_NOTIFY_KEY, nagiosExtension.getNotify());
        extensionMap.put(NAGIOS_EXTENSION_MAX_CHECK_ATTEMPTS_KEY, nagiosExtension.getMaxCheckAttempts());
        extensionMap.put(NAGIOS_EXTENSION_FRESHNESS_THRESHOLD_KEY, nagiosExtension.getFreshnessThresholdInSeconds());
        return extensionMap;
    }

    private final AtomicBoolean _isOpen = new AtomicBoolean(false);
    private final AlertQueryGenerator _alertQueryGenerator;

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseAlertRepository.class);
    private static final String NAGIOS_EXTENSION_SEVERITY_KEY = "severity";
    private static final String NAGIOS_EXTENSION_NOTIFY_KEY = "notify";
    private static final String NAGIOS_EXTENSION_MAX_CHECK_ATTEMPTS_KEY = "maxCheckAttempts";
    private static final String NAGIOS_EXTENSION_FRESHNESS_THRESHOLD_KEY = "freshnessThreshold";

    /**
     * Inteface for database query generation.
     */
    public interface AlertQueryGenerator {

        /**
         * Translate the <code>AlertQuery</code> to an Ebean <code>Query</code>.
         *
         * @param query The repository agnostic <code>AlertQuery</code>.
         * @return The database specific <code>PagedList</code> query result.
         */
        PagedList<models.ebean.Alert> createAlertQuery(AlertQuery query);

        /**
         * Save the <code>Alert</code> to the database. This needs to be executed in a transaction.
         *
         * @param alert The <code>Alert</code> model instance to save.
         */
        void saveAlert(models.ebean.Alert alert);
    }

    /**
     * RDBMS agnostic query for alerts using 'like'.
     */
    public static final class GenericQueryGenerator implements AlertQueryGenerator {

        /**
         * {@inheritDoc}
         */
        @Override
        public PagedList<models.ebean.Alert> createAlertQuery(final AlertQuery query) {
            ExpressionList<models.ebean.Alert> ebeanExpressionList = Ebean.find(models.ebean.Alert.class).where();
            if (query.getCluster().isPresent()) {
                ebeanExpressionList = ebeanExpressionList.eq("cluster", query.getCluster().get());
            }
            if (query.getContext().isPresent()) {
                ebeanExpressionList = ebeanExpressionList.eq("context", query.getContext().get().toString());
            }
            if (query.getService().isPresent()) {
                ebeanExpressionList = ebeanExpressionList.eq("service", query.getService().get());
            }

            final Query<models.ebean.Alert> ebeanQuery = ebeanExpressionList.query();
            int pageOffset = 0;
            if (query.getOffset().isPresent()) {
                pageOffset = query.getOffset().get() / query.getLimit();
            }
            return ebeanQuery.findPagedList(pageOffset, query.getLimit());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void saveAlert(final models.ebean.Alert alert) {
            Ebean.save(alert);
        }
    }
}
