/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.dashboard.story;

import java.util.List;

/**
 *
 * @author plamere
 */
public class TagInfo {
    private String tagName;
    private float score;
    private List<ScoredString> docTerms;
    private List<ScoredString> topTerms;

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        System.out.println("Set Tag Name " + tagName);
        this.tagName = tagName;
    }

    public List<ScoredString> getDocTerms() {
        return docTerms;
    }

    public void setDocTerms(List<ScoredString> docTerms) {
        this.docTerms = docTerms;
    }

    public List<ScoredString> getTopTerms() {
        return topTerms;
    }

    public void setTopTerms(List<ScoredString> topTerms) {
        this.topTerms = topTerms;
    }

}
