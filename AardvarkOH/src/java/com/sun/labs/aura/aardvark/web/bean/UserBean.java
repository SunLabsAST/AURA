
package com.sun.labs.aura.aardvark.web.bean;

import com.sun.labs.aura.aardvark.BlogUser;

/**
 * A class that holds info about a particular user
 */
public class UserBean {

    private String ID;
    
    private String nickname;
    
    private String defaultFeedURL;
    
    private String fullname;
    
    protected String emailAddress;
    
    private String recommendedFeedURL;
    
    protected String[] basisFeeds;
    
    public UserBean() {
        
    }
    
    public UserBean(BlogUser buser) {
        ID = buser.getKey();
        nickname = buser.getNickname();
        fullname = buser.getFullname();
        emailAddress = buser.getEmailAddress();
    }

    public UserBean(BlogUser buser, String url) {
        this(buser);
        defaultFeedURL = url;
    }


    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String realName) {
        this.nickname = realName;
    }

    public String getDefaultFeedURL() {
        return defaultFeedURL;
    }

    public void setDefaultFeedURL(String defaultFeedURL) {
        this.defaultFeedURL = defaultFeedURL;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getRecommendedFeedURL() {
        return recommendedFeedURL;
    }

    public void setRecommendedFeedURL(String recommendedFeedURL) {
        this.recommendedFeedURL = recommendedFeedURL;
    }

    public String[] getBasisFeeds() {
        return basisFeeds;
    }

    public void setBasisFeeds(String[] basisFeeds) {
        this.basisFeeds = basisFeeds;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
}
