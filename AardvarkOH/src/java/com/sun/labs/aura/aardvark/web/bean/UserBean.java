
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
    
    private String recommendedFeedURL;
    
    public UserBean() {
        
    }

    public UserBean(BlogUser buser, String url) {
        ID = buser.getKey();
        nickname = buser.getNickname();
        fullname = buser.getFullname();
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
}
