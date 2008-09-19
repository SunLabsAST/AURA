
package com.sun.labs.aura.dbbrowser.client.viz;

import java.io.Serializable;

/**
 *
 */
public class RepStats implements Serializable {
    protected double attentionsPerSec;
    protected double newItemsPerSec;
    protected double updatedItemsPerSec;
    protected double getItemsPerSec;
    protected double findSimsPerSec;

    public double getAttentionsPerSec() {
        return attentionsPerSec;
    }

    public void setAttentionsPerSec(double attentionsPerSec) {
        this.attentionsPerSec = attentionsPerSec;
    }

    public double getNewItemsPerSec() {
        return newItemsPerSec;
    }

    public void setNewItemsPerSec(double newItemsPerSec) {
        this.newItemsPerSec = newItemsPerSec;
    }

    public double getUpdatedItemsPerSec() {
        return updatedItemsPerSec;
    }

    public void setUpdatedItemsPerSec(double updatedItemsPerSec) {
        this.updatedItemsPerSec = updatedItemsPerSec;
    }

    public double getGetItemsPerSec() {
        return getItemsPerSec;
    }

    public void setGetItemsPerSec(double getItemsPerSec) {
        this.getItemsPerSec = getItemsPerSec;
    }

    public double getFindSimsPerSec() {
        return findSimsPerSec;
    }

    public void setFindSimsPerSec(double findSimsPerSec) {
        this.findSimsPerSec = findSimsPerSec;
    }
}
