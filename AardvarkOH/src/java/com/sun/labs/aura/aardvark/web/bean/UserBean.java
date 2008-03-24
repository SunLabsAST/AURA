
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
    
    private Long numFeeds;
    
    public UserBean() {
        
    }

    public UserBean(BlogUser buser, String url) {
        ID = buser.getKey();
        nickname = buser.getNickname();
        defaultFeedURL = url;
    }


    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getRealName() {
        return nickname;
    }

    public void setRealName(String realName) {
        this.nickname = realName;
    }

    public String getDefaultFeedURL() {
        return defaultFeedURL;
    }

    public void setDefaultFeedURL(String defaultFeedURL) {
        this.defaultFeedURL = defaultFeedURL;
    }

    public Long getNumFeeds() {
        return numFeeds;
    }

    public void setNumFeeds(Long numFeeds) {
        this.numFeeds = numFeeds;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }
}
