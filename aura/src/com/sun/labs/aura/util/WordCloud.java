/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.util;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public void set(String word, double weight) {
        Scored<String> s = words.get(word);
        if(s == null) {
            s = new Scored(word, weight);
            words.put(word, s);
        } else {
            s.setScore(weight);
        }
    }
    
    public Scored<String> getWord(String word) {
        return words.get(word);
    }

    public void remove(String word) {
        words.remove(word);
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

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Scored<String> s : this) {
            sb.append("(");
            sb.append(s.getItem());
            sb.append(",");
            sb.append(s.getScore());
            sb.append(")");
        }
        return sb.toString();
    }

    /**
     * Converts a string representation of a wordcloud to a WordCloud. The string
     * can be of the form '(tag, weight)(tag, weight)
     * or 'tag,tag,tag'
     * @param wc the string representation
     * @return a wordcloud or null
     */
    public static WordCloud convertStringToWordCloud(String wc) {
        WordCloud cloud = new WordCloud();
        Pattern pattern = Pattern.compile("(\\(([^,]*),\\s*([\\d\\.]+)\\s*\\))");
        Matcher matcher = pattern.matcher(wc);
        while (matcher.find()) {
            String tag = matcher.group(2).trim();
            String sweight = matcher.group(3).trim();
            try {
                float weight = Float.parseFloat(sweight);
                cloud.add(tag, weight);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        // if we couldn't find anything with the complex pattern, try
        // the simple version
        if (cloud.getWords().size() == 0) {
            String[] tags = wc.split(",");
            for (String tag : tags) {
                cloud.add(tag.trim(), 1f);
            }
        }

        return cloud.getWords().size() > 0 ? cloud : null;
    }

    private static void test(String wc) {
        WordCloud cloud = convertStringToWordCloud(wc);
        System.out.println("Text : " + wc);
        System.out.println("Cloud: " + cloud);
    }

    public static void main(String[] args) {
        test("(t1,.1)(t2, .2)(t 3, .3)");
        test("(this is a test,.1)(t2, .2)(t 3, .3)");
        test("(badcloud)");
        test("emo, punk, jazz,metal");
    }
}
