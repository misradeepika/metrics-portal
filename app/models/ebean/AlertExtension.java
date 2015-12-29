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
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.Version;

/**
 * Abstract model class for extensions data for alerts.
 *
 * @author Deepika Misra (deepika at groupon dot com)
 */
@MappedSuperclass
public abstract class AlertExtension extends Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long _id;

    @Version
    @Column(name = "version")
    private Long _version;

    @CreatedTimestamp
    @Column(name = "created_at")
    private Timestamp _createdAt;

    @UpdatedTimestamp
    @Column(name = "updated_at")
    private Timestamp _updatedAt;

    @OneToOne
    @JoinColumn(name = "id")
    private Alert _alert;

    public Alert getAlert() {
        return _alert;
    }

    public void setAlert(final Alert value) {
        _alert = value;
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
        _createdAt = value;
    }

    public Timestamp getUpdatedAt() {
        return _updatedAt;
    }

    public void setUpdatedAt(final Timestamp value) {
        _updatedAt = value;
    }
}
