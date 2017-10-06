package org.apereo.cas.mgmt.services.web.beans;

import java.io.Serializable;

/**
 * Created by tsschmi on 5/2/17.
 */
public class BranchActionData implements Serializable {
    BranchData branch;
    String note;

    BranchActionData() {

    }

    public BranchData getBranch() {
        return branch;
    }

    public void setBranch(final BranchData branch) {
        this.branch = branch;
    }

    public String getNote() {
        return note;
    }

    public void setNote(final String note) {
        this.note = note;
    }
}
