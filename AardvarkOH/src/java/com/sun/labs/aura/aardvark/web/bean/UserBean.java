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
