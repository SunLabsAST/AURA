/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.items;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 *
 * @author mailletf
 */
public class ListenerDetails implements IsSerializable {

    public enum Gender { Male, Female, Unknown };
    
    private ArtistDetails[] favArtistDetails;

    private String openID;
    private String userKey; // encrypted key given by the server for identifying a logged in user
    private boolean loggedIn=false;

    private String realName;
    private String nickName;
    private String birthDate;
    private String email;
    private String state;
    private String country;
    private Gender gender;
    private String language;

    private String lastfmUser;
    private String pandoraUser;

    private ArtistCompact[] recommendations;
    private ItemInfo[] userTagCloud;
    
    public void setArtistDetails(ArtistDetails[] favArtistDetails) {
        this.favArtistDetails = favArtistDetails;
    }
    
    public ArtistDetails[] getArtistDetails() {
        return favArtistDetails;
    }
    
    public void setOpenId(String openID) {
        this.openID = openID;
    }
    
    public String getOpenId() {
        return openID;
    }
    
    public void setIsLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }
    
    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }
    
    public String getRealName() {
        return realName;
    }
    
    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
    
    public String getNickName() {
        return nickName;
    }
    
    public void setBirhtDate(String birthDate) {
        this.birthDate = birthDate;
    }
    
    public String getBirthDate() {
        return birthDate;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getState() {
        return state;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public String getCountry() {
        return country;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    /**
     * Sets the listener's gender by trying to match a string value to one of the
     * Gender values
     * @param gender
     * @return true if gender matched or false if gender was not set
     */
    public boolean setGender(String gender) {
        for (Gender g : Gender.values()) {
            if (gender.equals(g.toString())) {
                this.gender = g;
                return true;
            }
        }
        return false;
    }
    
    public void setGender(Gender gender) {
        this.gender = gender;
    }
    
    public Gender getGender() {
        return gender;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public String getLanguage() {
        return language;
    }

    public void setLastFmUser(String lastfmUser) {
        this.lastfmUser = lastfmUser;
    }

    public String getLastFmUser() {
        return lastfmUser;
    }

    public void setPandoraUser(String pandoraUser) {
        this.pandoraUser = pandoraUser;
    }

    public String getPandoraUser() {
        return pandoraUser;
    }

    public void setRecommendations(ArtistCompact[] recommendations) {
        this.recommendations = recommendations;
    }

    public ArtistCompact[] getRecommendations() {
        return recommendations;
    }

    public void setUserTagCloud(ItemInfo[] userTagCloud) {
        this.userTagCloud = userTagCloud;
    }

    public ItemInfo[] getUserTagCloud() {
        return userTagCloud;
    }
}