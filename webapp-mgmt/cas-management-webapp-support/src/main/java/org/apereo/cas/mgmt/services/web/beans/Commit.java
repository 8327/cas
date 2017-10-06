package org.apereo.cas.mgmt.services.web.beans;

/**
 * Created by tsschmi on 3/17/17.
 */
public class Commit {
    String id;
    String text;

    public Commit() {

    }

    public Commit(final String id, final String text) {
        this.id = id;
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = text;
    }
}
