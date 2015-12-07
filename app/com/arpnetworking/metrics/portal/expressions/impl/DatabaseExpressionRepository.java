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

import com.arpnetworking.metrics.portal.expressions.ExpressionRepository;
import com.arpnetworking.play.configuration.ConfigurationHelper;
import com.arpnetworking.steno.Logger;
import com.arpnetworking.steno.LoggerFactory;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.PagedList;
import com.avaje.ebean.Query;
import com.avaje.ebean.Transaction;
import com.google.inject.Inject;
import models.internal.Expression;
import models.internal.ExpressionQuery;
import models.internal.QueryResult;
import models.internal.impl.DefaultExpression;
import models.internal.impl.DefaultExpressionQuery;
import models.internal.impl.DefaultQueryResult;
import play.Configuration;
import play.Environment;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Implementation of <code>ExpressionRepository</code> using SQL database.
 *
 * @author Deepika Misra (deepika at groupon dot com)
 */
public class DatabaseExpressionRepository implements ExpressionRepository {

    /**
     * Public constructor.
     *
     * @param environment Play's <code>Environment</code> instance.
     * @param config      Play's <code>Configuration</code> instance.
     * @throws Exception If the configuration is invalid.
     */
    @Inject
    public DatabaseExpressionRepository(final Environment environment, final Configuration config) throws Exception {
        this(
                ConfigurationHelper.<ExpressionQueryGenerator>getType(
                        environment,
                        config,
                        "expressionRepository.expressionQueryGenerator.type")
                        .newInstance());
    }

    /**
     * Public constructor.
     *
     * @param expressionQueryGenerator Instance of <code>ExpressionQueryGenerator</code>.
     */
    public DatabaseExpressionRepository(final ExpressionQueryGenerator expressionQueryGenerator) {
        _expressionQueryGenerator = expressionQueryGenerator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void open() {
        assertIsOpen(false);
        LOGGER.debug().setMessage("Opening expression repository").log();
        _isOpen.set(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        assertIsOpen();
        LOGGER.debug().setMessage("Closing expression repository").log();
        _isOpen.set(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Expression> get(final UUID identifier) {
        assertIsOpen();
        LOGGER.debug()
                .setMessage("Getting expression")
                .addData("expressionId", identifier)
                .log();

        final models.ebean.Expression ebeanExpression = Ebean.find(models.ebean.Expression.class)
                .where()
                .eq("id", identifier)
                .findUnique();
        if (ebeanExpression == null) {
            return Optional.empty();
        }

        final Expression result = new DefaultExpression.Builder()
                .setId(ebeanExpression.getId())
                .setCluster(ebeanExpression.getCluster())
                .setMetric(ebeanExpression.getMetric())
                .setScript(ebeanExpression.getScript())
                .setService(ebeanExpression.getService())
                .build();
        return Optional.of(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExpressionQuery createQuery() {
        assertIsOpen();
        LOGGER.debug().setMessage("Preparing query").log();
        return new DefaultExpressionQuery(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueryResult<Expression> query(final ExpressionQuery query) {
        assertIsOpen();
        LOGGER.debug()
                .setMessage("Querying")
                .addData("query", query)
                .log();

        // Create the base query
        final PagedList<models.ebean.Expression> pagedExpressions = _expressionQueryGenerator.createExpressionQuery(query);

        // Compute the etag
        // NOTE: Another way to do this would be to use the version field and hash those together.
        final String etag = Long.toHexString(pagedExpressions.getList().stream()
                .map(expression -> expression
                        .getUpdatedAt()
                        .after(expression.getCreatedAt()) ? expression.getUpdatedAt() : expression.getCreatedAt())
                .max(Timestamp::compareTo)
                .orElse(new Timestamp(0))
                .getTime());

        // Transform the results
        return new DefaultQueryResult<>(
                pagedExpressions.getList()
                        .stream()
                        .map(expression -> new DefaultExpression.Builder()
                                .setCluster(expression.getCluster())
                                .setId(expression.getId())
                                .setMetric(expression.getMetric())
                                .setService(expression.getService())
                                .setScript(expression.getScript())
                                .build())
                        .collect(Collectors.toList()),
                pagedExpressions.getTotalRowCount(),
                etag);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getExpressionCount() {
        assertIsOpen();
        return Ebean.find(models.ebean.Expression.class)
                .findRowCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addOrUpdateExpression(final Expression expression) {
        assertIsOpen();
        LOGGER.debug()
                .setMessage("Upserting expression")
                .addData("expression", expression)
                .log();

        final Transaction transaction = Ebean.beginTransaction();

        try {
            models.ebean.Expression ebeanExpression = models.ebean.Expression.FIND.byId(expression.getId());
            boolean created = false;
            if (ebeanExpression == null) {
                ebeanExpression = new models.ebean.Expression();
                created = true;
            }

            ebeanExpression.setCluster(expression.getCluster());
            ebeanExpression.setId(expression.getId());
            ebeanExpression.setMetric(expression.getMetric());
            ebeanExpression.setScript(expression.getScript());
            ebeanExpression.setService(expression.getService());
            _expressionQueryGenerator.saveHost(ebeanExpression);
            transaction.commit();

            LOGGER.info()
                    .setMessage("Upserted expression")
                    .addData("expression", expression)
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
                .setMessage("Deleting all expression")
                .log();
        final List<models.ebean.Expression> exprs = Ebean.find(models.ebean.Expression.class).findList();
        final Transaction transaction = Ebean.beginTransaction();
        try {
            Ebean.delete(exprs);
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
            throw new IllegalStateException(String.format("Expression repository is not %s", expectedState ? "open" : "closed"));
        }
    }

    private final AtomicBoolean _isOpen = new AtomicBoolean(false);
    private final ExpressionQueryGenerator _expressionQueryGenerator;

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseExpressionRepository.class);

    /**
     * Inteface for database query generation.
     */
    public interface ExpressionQueryGenerator {

        /**
         * Translate the <code>ExpressionQuery</code> to an Ebean <code>Query</code>.
         *
         * @param query The repository agnostic <code>ExpressionQuery</code>.
         * @return The database specific <code>PagedList</code> query result.
         */
        PagedList<models.ebean.Expression> createExpressionQuery(ExpressionQuery query);

        /**
         * Save the <code>Expression</code> to the database. This needs to be executed in a transaction.
         *
         * @param expression The <code>Expression</code> model instance to save.
         */
        void saveHost(models.ebean.Expression expression);
    }

    /**
     * RDBMS agnostic query for expressions using 'like'.
     */
    public static final class GenericQueryGenerator implements ExpressionQueryGenerator {

        /**
         * {@inheritDoc}
         */
        @Override
        public PagedList<models.ebean.Expression> createExpressionQuery(final ExpressionQuery query) {
            ExpressionList<models.ebean.Expression> ebeanExpressionList = Ebean.find(models.ebean.Expression.class).where();
            if (query.getCluster().isPresent()) {
                ebeanExpressionList = ebeanExpressionList.eq("cluster", query.getCluster().get());
            }
            if (query.getService().isPresent()) {
                ebeanExpressionList = ebeanExpressionList.eq("service", query.getService().get().toString());
            }

            final Query<models.ebean.Expression> ebeanQuery = ebeanExpressionList.query();

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
        public void saveHost(final models.ebean.Expression expression) {
            Ebean.save(expression);
        }
    }
}
