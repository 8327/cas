package org.apereo.cas.mgmt.services.web.beans;

import org.eclipse.jgit.lib.ObjectId;

import java.io.Serializable;

/**
 * Created by tsschmi on 3/17/17.
 */
public class Diff implements Serializable {
    String oldId;
    String newId;
    String path;
    String changeType;

    public Diff(){

    }

    public Diff(String path, ObjectId oldId, ObjectId newId, String changeType) {
        this.path = path;
        this.oldId = ObjectId.toString(oldId);
        this.newId = ObjectId.toString(newId);
        this.changeType = changeType;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }
}
