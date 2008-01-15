/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.search.music.web.apml;

import com.sun.labs.search.music.web.lastfm.Item;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author plamere
 */
public class APML {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private String title;
    private String defaultProfile;
    private Map<String, Profile> profileMap;

    public APML(String title) {
        this.title = title;
        profileMap = new HashMap<String, Profile>();
    }

    public String getTitle() {
        return title;
    }

    public String getDefaultProfile() {
        return defaultProfile;
    }

    public void setDefaultProfile(String defaultProfile) {
        this.defaultProfile = defaultProfile;
    }

    public void addProfile(Profile profile) {
        if (defaultProfile == null) {
            defaultProfile = profile.getName();
        }
        profileMap.put(profile.getName(), profile);
    }

    public Profile getProfile(String profileName) {
        return profileMap.get(profileName);
    }

    public Set<String> getProfileNames() {
        return profileMap.keySet();
    }

    static Concept[] getExplicitConceptsFromItems(Item[] artists) {
        List<Concept> conceptList = new ArrayList<Concept>();
        Item mostFrequentArtist = findMostFrequentItem(artists);
        for (Item artist : artists) {
            Concept concept = new Concept(artist.getName(), artist.getFreq() / (float) mostFrequentArtist.getFreq());
            conceptList.add(concept);
        }
        return normalizeAndPrune(conceptList, 0);
    }

    static Concept[] normalizeAndPrune(List<Concept> conceptList, float minValue) {
        Collections.sort(conceptList);
        Collections.reverse(conceptList);

        float maxValue = 1.0f;
        if (conceptList.size() > 0) {
            maxValue = conceptList.get(0).getValue();
        }

        int lastIndex = 0;
        for (Concept c : conceptList) {
            c.setValue(c.getValue() / maxValue);
            if (c.getValue() < minValue) {
                break;
            }

            lastIndex++;
        }

        conceptList = conceptList.subList(0, lastIndex);
        return conceptList.toArray(new Concept[0]);
    }

    static Item findMostFrequentItem(Item[] items) {
        Item maxItem = null;

        for (Item item : items) {
            if (maxItem == null || item.getFreq() > maxItem.getFreq()) {
                maxItem = item;
            }

        }
        return maxItem;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\"?>\n");
        sb.append("<APML xmlns=\"http://www.apml.org/apml-0.6\" version=\"0.6\" >\n");
        sb.append("<Head>\n");
        if (getTitle() != null) {
            sb.append("   <Title>" + getTitle() + "</Title>\n");
        }
        sb.append("   <Generator>Created by TasteBroker.org </Generator>\n");
        sb.append("   <DateCreated>" + sdf.format(new Date()) + "</DateCreated>\n");
        sb.append("</Head>\n");

        sb.append("<Body defaultprofile=\"" + getDefaultProfile() + "\">\n");

        for (String profileName : getProfileNames()) {
            Profile profile = getProfile(profileName);
            sb.append(profile.toString());
        }
        sb.append("</Body>\n");
        sb.append("</APML>\n");
        return sb.toString();
    }
}
