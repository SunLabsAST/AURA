/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.dashboard.story;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author plamere
 */
public class TagInfo {
    private final static List<ScoredString> EMPTY 
                = Collections.unmodifiableList(new ArrayList<ScoredString>());
    private String tagName;
    private float score;
    private List<ScoredString> docTerms = EMPTY;
    private List<ScoredString> topTerms = EMPTY;
    private List<ScoredString> simTags = EMPTY;

    private List<Story> similarStories;
    private List<Story> similarTagInfos;

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

    public List<ScoredString> getSimTags() {
        return simTags;
    }

    public void setSimTags(List<ScoredString> simTags) {
        this.simTags = simTags;
    }

    public List<Story> getSimilarStories() {
        return similarStories;
    }

    public void setSimilarStories(List<Story> similarStories) {
        this.similarStories = similarStories;
    }

    public List<Story> getSimilarTagInfos() {
        return similarTagInfos;
    }

    public void setSimilarTagInfos(List<Story> similarTagInfos) {
        this.similarTagInfos = similarTagInfos;
    }

    public void dump() {
        System.out.println("Tag " + tagName + " score " + score);
        dumpList("docTerms", docTerms);
        dumpList("topTerms", topTerms);
        dumpList("simTags", simTags);
    }
    
    private void dumpList(String name, List<ScoredString> list) {
        System.out.println(name);
        for (ScoredString s : list) {
            System.out.println("    " + s.getName() + " " + s.getScore());
        }
    }
}
