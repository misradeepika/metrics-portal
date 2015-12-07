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
package models.ebean;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.avaje.ebean.annotation.UpdatedTimestamp;

import java.sql.Timestamp;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 * Data model for alerts.
 *
 * @author Deepika Misra (deepika at groupon dot com)
 */
@Entity
@Table(name = "alerts", schema = "portal")
public class Alert extends Model {

    @Id
    @Column(name = "id")
    private UUID _id;

    @Version
    @Column(name = "version")
    private Long _version;

    @CreatedTimestamp
    @Column(name = "created_at")
    private Timestamp _createdAt;

    @UpdatedTimestamp
    @Column(name = "updated_at")
    private Timestamp _updatedAt;

    @Column(name = "name")
    private String _name;

    @Column(name = "cluster")
    private String _cluster;

    @Column(name = "service")
    private String _service;

    @Column(name = "context")
    private String _context;

    @Column(name = "metric")
    private String _metric;

    @Column(name = "statistic")
    private String _statistic;

    @Column(name = "period")
    private String _period;

    @Column(name = "operator")
    private String _operator;

    @Column(name = "quantity")
    private String _quantity;

    @Column(name = "extensions")
    private String _extensions;

    public UUID getId() {
        return _id;
    }

    public void setId(final UUID value) {
        _id = value;
    }

    public Long getVersion() {
        return _version;
    }

    public void setVersion(final Long version) {
        _version = version;
    }

    public Timestamp getCreatedAt() {
        return _createdAt;
    }

    public void setCreatedAt(final Timestamp value) {
        this._createdAt = value;
    }

    public Timestamp getUpdatedAt() {
        return _updatedAt;
    }

    public void setUpdatedAt(final Timestamp value) {
        this._updatedAt = value;
    }

    public String getName() {
        return _name;
    }

    public void setName(final String value) {
        _name = value;
    }

    public String getCluster() {
        return _cluster;
    }

    public void setCluster(final String value) {
        _cluster = value;
    }

    public String getService() {
        return _service;
    }

    public void setService(final String value) {
        _service = value;
    }

    public String getContext() {
        return _context;
    }

    public void setContext(final String value) {
        _context = value;
    }

    public String getMetric() {
        return _metric;
    }

    public void setMetric(final String value) {
        _metric = value;
    }

    public String getStatistic() {
        return _statistic;
    }

    public void setStatistic(final String value) {
        _statistic = value;
    }

    public String getPeriod() {
        return _period;
    }

    public void setPeriod(final String value) {
        _period = value;
    }

    public String getOperator() {
        return _operator;
    }

    public void setOperator(final String value) {
        _operator = value;
    }

    public String getQuantity() {
        return _quantity;
    }

    public void setQuantity(final String value) {
        _quantity = value;
    }

    public String getExtensions() {
        return _extensions;
    }

    public void setExtensions(final String value) {
        _extensions = value;
    }

    /**
     * Finder function for <code>Alert</code> entity.
     */
    public static final Finder<UUID, Alert> FIND = new Finder<>(Alert.class);
}
