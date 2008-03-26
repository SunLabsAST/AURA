/*
 *  Copyright 2007 Sun Microsystems, Inc. 
 *  All Rights Reserved. Use is subject to license terms.
 * 
 *  See the file "license.terms" for information on usage and
 *  redistribution of this file, and for a DISCLAIMER OF ALL
 *  WARRANTIES..
 */

package com.sun.labs.aura.aardvark;

import com.sun.labs.aura.util.ItemAdapter;
import com.sun.labs.aura.datastore.StoreFactory;
import com.sun.labs.aura.datastore.User;

/**
 *
 */
public class BlogUser extends ItemAdapter {
    private final static String FIELD_EMAIL_ADDRESS = "email";
    private final static String FIELD_NICKNAME = "nickname";
    private final static String FIELD_FULLNAME = "fullname";
    private final static String FIELD_DOB = "dob";
    private final static String FIELD_GENDER = "gender";
    private final static String FIELD_POSTCODE = "postcode";
    private final static String FIELD_COUNTRY = "country";
    private final static String FIELD_LANGUAGE = "language";
    private final static String FIELD_TIMEZONE = "timezone";
    
    
    /**
     * Wraps a SimpleItem as blog user (someone who reads blogs)
     * @param item the item to be turned into a blog entry
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public BlogUser(User user) {
        super(user); // TBD - there's no USER type right now
    }

    /**
     * Creates a new blog entry
     * @param key the key for the blog entry
     * @param name the name of the blog entry
     * @throws com.sun.labs.aura.aardvark.util.AuraException
     */
    public BlogUser(String key, String name) {
        this(StoreFactory.newUser(key, name));
    }

    public String getRandString() {
        return ((User)item).getUserRandString();
    }
    
    /**
     * Sets the email address of the user
     * @param address the email address
     */
    public void setEmailAddress(String address) {
        setField(FIELD_EMAIL_ADDRESS, address);
    }

    /**
     * Returns the email address of the user
     * @return the email addresss
     */
    public String getEmailAddress() {
        return getFieldAsString(FIELD_EMAIL_ADDRESS);
    }


    public String getNickname() {
        return getFieldAsString(FIELD_NICKNAME);
    }

    public void setNickname(String nickname) {
        setField(FIELD_NICKNAME, nickname);
    }

    public String getFullname() {
        return getFieldAsString(FIELD_FULLNAME);
    }

    public void setFullname(String fullname) {
        setField(FIELD_FULLNAME, fullname);
    }

    public String getDob() {
        return getFieldAsString(FIELD_DOB);
    }

    public void setDob(String dob) {
        setField(FIELD_DOB, dob);
    }

    public String getGender() {
        return getFieldAsString(FIELD_GENDER);
    }

    public void setGender(String gender) {
        setField(FIELD_GENDER, gender);
    }

    public String getPostcode() {
        return getFieldAsString(FIELD_POSTCODE);
    }

    public void setPostcode(String postcode) {
        setField(FIELD_POSTCODE, postcode);
    }

    public String getCountry() {
        return getFieldAsString(FIELD_COUNTRY);
    }

    public void setCountry(String country) {
        setField(FIELD_COUNTRY, country);
    }

    public String getLanguage() {
        return getFieldAsString(FIELD_LANGUAGE);
    }

    public void setLanguage(String language) {
        setField(FIELD_LANGUAGE, language);
    }

    public String getTimezone() {
        return getFieldAsString(FIELD_TIMEZONE);
    }

    public void setTimezone(String timezone) {
        setField(FIELD_TIMEZONE, timezone);
    }
    
}
