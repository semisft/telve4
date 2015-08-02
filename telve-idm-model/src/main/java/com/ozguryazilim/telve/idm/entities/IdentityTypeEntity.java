/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
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
package com.ozguryazilim.telve.idm.entities;

import org.picketlink.idm.jpa.annotations.AttributeValue;
import org.picketlink.idm.jpa.annotations.IdentityClass;
import org.picketlink.idm.jpa.annotations.OwnerReference;
import org.picketlink.idm.jpa.annotations.entity.IdentityManaged;
import org.picketlink.idm.model.IdentityType;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

/**
 * @author pedroigor
 */
@IdentityManaged (IdentityType.class)
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "PL_IDENTITY")
public class IdentityTypeEntity extends AttributedTypeEntity {

    private static final long serialVersionUID = -6533395974259723600L;

    @IdentityClass
    @Column(name = "TYPE_NAME")
    private String typeName;

    @Temporal(TemporalType.TIMESTAMP)
    @AttributeValue
    @Column(name = "CREATE_DATE")
    private Date createdDate;

    @Temporal(TemporalType.TIMESTAMP)
    @AttributeValue
    @Column(name = "EXPIRE_DATE")
    private Date expirationDate;

    @AttributeValue
    @Column(name = "ENABLED")
    private boolean enabled;

    @OwnerReference
    @ManyToOne
    @JoinColumn(name = "PARTITION_ID")
    private PartitionTypeEntity partition;

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public PartitionTypeEntity getPartition() {
        return partition;
    }

    public void setPartition(PartitionTypeEntity partition) {
        this.partition = partition;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!getClass().isInstance(obj)) {
            return false;
        }

        IdentityTypeEntity other = (IdentityTypeEntity) obj;

        return getId() != null && other.getId() != null && getId().equals(other.getId())
                && getTypeName() != null && other.getTypeName() != null && getTypeName().equals(other.getTypeName());
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getId() != null ? getId().hashCode() : 0);
        return result;
    }
}
