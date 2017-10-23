package org.apereo.cas.mgmt.services.web.beans;

import java.io.Serializable;

/**
 * Created by tsschmi on 3/17/17.
 */
public class Change implements Serializable {
    String id;
    String fileName;
    String changeType;
    String serviceName;
    String oldId;
    String newId;

    public Change(String id, String fileName, String changeType, String serviceName, String oldId, String newId) {
        this.id = id;
        this.fileName = fileName;
        this.changeType = changeType;
        this.serviceName = serviceName;
        this.newId = newId;
        this.oldId = oldId;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(final String changeType) {
        this.changeType = changeType;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getOldId() {
        return oldId;
    }

    public void setOldId(String oldId) {
        this.oldId = oldId;
    }

    public String getNewId() {
        return newId;
    }

    public void setNewId(String newId) {
        this.newId = newId;
    }
}
