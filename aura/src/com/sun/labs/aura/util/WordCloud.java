/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.util;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * A cloud of tags and weights.
 */
public class WordCloud implements Serializable, Iterable<Scored<String>> {

    private Map<String, Scored<String>> words;

    public WordCloud() {
        words = new TreeMap();
    }

    public void add(String word, double weight) {
        Scored<String> s = words.get(word);
        if(s == null) {
            s = new Scored(word, weight);
            words.put(word, s);
        } else {
            s.setScore(s.getScore() + weight);
        }
    }
    
    public Scored<String> getWord(String word) {
        return words.get(word);
    }
    
    public void add(Scored<String> s) {
        Scored<String> c = words.get(s.getItem());
        if(c == null) {
            words.put(s.getItem(), s);
        } else {
            c.setScore(c.getScore()+s.getScore());
        }
    }
    
    public Map<String, Scored<String>> getWords() {
        return words;
    }
    
    public void clear() {
        words.clear();
    }
    
    public int size() {
        return words.size();
    }

    /**
     * Returns an iterator for the words in this cloud.  The words are returned
     * in lexicographic order.
     * @return an iterator for this cloud.
     */
    public Iterator<Scored<String>> iterator() {
        return words.values().iterator();
    }
}
