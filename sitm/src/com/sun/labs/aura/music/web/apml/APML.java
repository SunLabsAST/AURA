/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.web.apml;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author plamere
 */
public class APML {

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
    
    
    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
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
