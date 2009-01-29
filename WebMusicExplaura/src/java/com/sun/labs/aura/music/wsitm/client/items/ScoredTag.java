/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.items;

/**
 *
 * @author mailletf
 */
public class ScoredTag extends ScoredC<String> {

    private boolean isSticky = false;

    public ScoredTag() {
        super();
    }

    public ScoredTag(String tag, double score, boolean isSticky) {
        super(tag, score);
        this.isSticky = isSticky;
    }

    public ScoredTag(String tag, double score) {
        super(tag, score);
    }

    public String getName() {
        return item;
    }

    public boolean isSticky() {
        return isSticky;
    }

    public void setSticky(boolean stick) {
        this.isSticky = stick;
    }

    public void setScore(double newScore) {
        this.score = newScore;
    }

    @Override
    public int hashCode() {
        return item.toLowerCase().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ScoredTag other = (ScoredTag) obj;
        if (item.toLowerCase().equals(other.item.toLowerCase())) {
            return true;
        } else {
            return false;
        }
    }
}