/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.util;

import com.sun.labs.aura.datastore.SimilarityConfig;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A cloud of tags and weights.
 */
public class WordCloud implements Serializable, Iterable<Scored<String>> {
    private Map<String, Scored<String>> words;
    private Set<String> stickyWords;
    private Set<String> bannedWords;

    public WordCloud() {
        words = new TreeMap();
        stickyWords = new HashSet<String>();
        bannedWords = new HashSet<String>();
    }

    public void add(String word, double weight) {
        Scored<String> s = words.get(word);
        if (s == null) {
            s = new Scored(word, weight);
            words.put(word, s);
        } else {
            s.setScore(s.getScore() + weight);
        }
    }

    public void addStickyWord(String word) {
        stickyWords.add(word);
    }

    public Set<String> getStickyWords() {
        return Collections.unmodifiableSet(stickyWords);
    }

    public void removeStickyWord(String word) {
        stickyWords.remove(word);
    }

    public boolean isSticky(String word) {
        return stickyWords.contains(word);
    }

    public void clearStickyWord(String word) {
        stickyWords.clear();
    }

    public void addBannedWord(String word) {
        bannedWords.add(word);
    }

    public void removeBannedWord(String word) {
        bannedWords.remove(word);
    }

    public void clearBannedWord(String word) {
        bannedWords.clear();
    }

    public boolean isBanned(String word) {
        return bannedWords.contains(word);
    }

    public Set<String> getBannedWords() {
        return Collections.unmodifiableSet(bannedWords);
    }

    public void set(String word, double weight) {
        Scored<String> s = words.get(word);
        if (s == null) {
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
        if (c == null) {
            words.put(s.getItem(), s);
        } else {
            c.setScore(c.getScore() + s.getScore());
        }
    }

    public Map<String, Scored<String>> getWords() {
        return words;
    }

    /**
     * Gets the exclusion terms from this world cloud and assigns them into the 
     * similarity config.
     * @param config the similarity config that we want to modify.
     */
    public void getExcluded(SimilarityConfig config) {
        Set<String> exc = getBannedWords();
        if (exc.size() > 0) {
            config.setExclude(exc);
        }
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

            if (isSticky(s.getItem())) {
                sb.append("+");
            }

            if (isBanned(s.getItem())) {
                sb.append("-");
            }
            sb.append(s.getItem());
            sb.append(",");
            sb.append(String.format("%.2g", s.getScore()));
            sb.append(")");
        }
        return sb.toString();
    }

    /**
     * Converts a string representation of a wordcloud to a WordCloud. The string
     * can be of the form '(tag, weight)(+tag, weight)(-tag)(-tag, -.1)(tag)(+tag)
     * can be of the form '(tag, weight)(+tag, weight)(-tag)
     * or 'tag,tag,+tag'
     * or 'tag,tag,+tag,-tag'
     * + indicates a sticky tag, - indicates a banned tag
     * @param wc the string representation
     * @return a wordcloud or null
     */
    public static WordCloud convertStringToWordCloud(String wc) {
        WordCloud cloud = new WordCloud();
        Pattern pattern = Pattern.compile("(\\(([^,\\)]*)(,\\s*(-*[\\d\\.]+)\\s*)*)\\)");
        Matcher matcher = pattern.matcher(wc);
        while (matcher.find()) {
            String tag = matcher.group(2).trim();
            String sweight = "1";

            if (matcher.groupCount() > 3) {
                String s = matcher.group(4);
                if (s != null) {
                    sweight = s.trim();
                }
            }

            String normTag = norm(tag);

            if (hasStickyPrefix(tag)) {
                cloud.addStickyWord(normTag);
            }

            if (hasBannedPrefix(tag)) {
                cloud.addBannedWord(normTag);
            }

            // we force the sign of the weight to match the sticky/ban class

            try {
                float weight = Float.parseFloat(sweight);

                if (cloud.isSticky(normTag)) {
                    weight = Math.abs(weight);
                } else if (cloud.isBanned(normTag)) {
                    weight = -Math.abs(weight);
                }
                cloud.add(normTag, weight);
            } catch (NumberFormatException e) {
                return null;
            }
        }


        // if we couldn't find anything with the complex pattern, try
        // the simple version
        if (cloud.getWords().size() == 0) {
            String[] tags = wc.split(",");
            for (String tag : tags) {
                tag = tag.trim();
                String normTag = norm(tag);
                if (hasStickyPrefix(tag)) {
                    cloud.addStickyWord(normTag);
                }

                if (hasBannedPrefix(tag)) {
                    cloud.addBannedWord(normTag);
                } 
                cloud.add(normTag, 1f);
            }
        }

        return cloud.getWords().size() > 0 ? cloud : null;
    }

    private static boolean hasStickyPrefix(String tag) {
        return tag.startsWith("+");
    }

    private static boolean hasBannedPrefix(String tag) {
        return tag.startsWith("-");
    }

    private static String norm(String tag) {
        return tag.replaceFirst("[-\\+]+", "").trim();
    }

    private static void test(String wc) {
        WordCloud cloud = convertStringToWordCloud(wc);
        System.out.println("Text : " + wc);
        System.out.println("Cloud: " + cloud);
    }

    public static void main(String[] args) {
        test("(t1,.1)(t2)(t3, .3)(-t4)");
        test("(this is a test,.1)(t2, .2)(t 3, .3)");
        test("(this is a test,.1)(t2, .2)(gothic, -.3)");
        test("(+this is a test,.1)(t2, .2)(gothic, -.3)");
        test("(-this is a test,.1)(t2, .2)(gothic, -.3)");
        test("(simpleloud)");
        test("emo, -punk, jazz,metal");
        test("+emo, -punk, jazz,metal");
    }
}
