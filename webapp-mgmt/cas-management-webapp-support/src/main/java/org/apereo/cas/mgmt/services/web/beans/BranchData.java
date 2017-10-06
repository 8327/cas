package org.apereo.cas.mgmt.services.web.beans;

import java.io.Serializable;

/**
 * Created by tsschmi on 3/17/17.
 */
public class BranchData implements Serializable {
    public String name;
    public boolean accepted;
    public String msg;
    public String committer;
    public long time;
    public String id;
    public boolean rejected;
    public boolean reverted;

    public BranchData() {

    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(final String msg) {
        this.msg = msg;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getCommitter() {
        return committer;
    }

    public void setCommitter(final String committer) {
        this.committer = committer;
    }

    public long getTime() {
        return time;
    }

    public void setTime(final long time) {
        this.time = time;
    }

    public boolean isRejected() {
        return rejected;
    }

    public void setRejected(final boolean rejected) {
        this.rejected = rejected;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(final boolean accepted) {
        this.accepted = accepted;
    }

    public boolean isReverted() {
        return reverted;
    }

    public void setReverted(final boolean reverted) {
        this.reverted = reverted;
    }
}
