/*
 * Copyright 2007-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
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
