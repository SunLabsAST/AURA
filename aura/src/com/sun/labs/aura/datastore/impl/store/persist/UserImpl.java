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

package com.sun.labs.aura.datastore.impl.store.persist;

import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;
import com.sun.labs.aura.datastore.User;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.util.Date;

/**
 * Provides a user implementation that is persistent in the BDB.
 */
@SuppressWarnings(value="EQ_DOESNT_OVERRIDE_EQUALS",
                  justification="Definition of equality is the same")
@Persistent(version = 4)
public class UserImpl extends ItemImpl implements User {
    private static final long serialVersionUID = 4;

    /** Signal variable to indicate that this is a user, not a regular item */
    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    protected boolean isUser = true;
    
    @SecondaryKey(relate = Relationship.ONE_TO_ONE)
    protected String randStr = "";

    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    protected String nickname;

    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    protected String fullname;

    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    protected String email;

    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    protected Long dob;

    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    protected String gender;

    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    protected String postcode;

    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    protected String country;

    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    protected String language;

    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    protected String timezone;

    public UserImpl() {
        //
        // Default constructor for BDB
    }
    
    public UserImpl(String key, String name) {
        this.itemType = ItemType.USER.ordinal();
        this.key = key;
        this.name = name;
        this.typeAndTimeAdded = new IntAndTimeKey(this.itemType,
                                                  System.currentTimeMillis());
    }

    public String getUserRandString() {
        return randStr;
    }

    public void setUserRandString(String random) {
        this.randStr = random;
    }

    /**
     * @return the nickname
     */
    @Override
    public String getNickname() {
        return nickname;
    }

    /**
     * @param nickname the nickname to set
     */
    @Override
    public void setNickname(String nickname) {
        this.nickname = nickname;
        setField("nickname", nickname);
    }

    /**
     * @return the fullname
     */
    @Override
    public String getFullname() {
        return fullname;
    }

    /**
     * @param fullname the fullname to set
     */
    @Override
    public void setFullname(String fullname) {
        this.fullname = fullname;
        setField("fullname", fullname);
    }

    /**
     * @return the email
     */
    @Override
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    @Override
    public void setEmail(String email) {
        this.email = email;
        setField("email", email);
    }

    /**
     * @return the dob
     */
    @Override
    public Date getDob() {
        return new Date(dob);
    }

    /**
     * @param dob the dob to set
     */
    @Override
    public void setDob(Date dob) {
        this.dob = dob.getTime();
        setField("dob", dob);
    }

    /**
     * @return the gender
     */
    @Override
    public String getGender() {
        return gender;
    }

    /**
     * @param gender the gender to set
     */
    @Override
    public void setGender(String gender) {
        this.gender = gender;
        setField("gender", gender);
    }

    /**
     * @return the postcode
     */
    @Override
    public String getPostcode() {
        return postcode;
    }

    /**
     * @param postcode the postcode to set
     */
    @Override
    public void setPostcode(String postcode) {
        this.postcode = postcode;
        setField("postcode", postcode);
    }

    /**
     * @return the country
     */
    @Override
    public String getCountry() {
        return country;
    }

    /**
     * @param country the country to set
     */
    @Override
    public void setCountry(String country) {
        this.country = country;
        setField("country", country);
    }

    /**
     * @return the language
     */
    @Override
    public String getLanguage() {
        return language;
    }

    /**
     * @param language the language to set
     */
    @Override
    public void setLanguage(String language) {
        this.language = language;
        setField("language", language);
    }

    /**
     * @return the timezone
     */
    @Override
    public String getTimezone() {
        return timezone;
    }

    /**
     * @param timezone the timezone to set
     */
    @Override
    public void setTimezone(String timezone) {
        this.timezone = timezone;
        setField("timezone", timezone);
    }

    public static final transient UserImpl INVALID_USER =
            new UserImpl("a very unlikely user key to occur in the database",
                         "steve green is the steviest greeniest steve green");

    static {
        INVALID_USER.country = "some country that does not exist";
        INVALID_USER.dob = Long.MIN_VALUE;
        INVALID_USER.email = "** invalid email address **";
        INVALID_USER.fullname = "some name that isn't ever likely to exist";
        INVALID_USER.gender = "a third gender not found in our humans";
        INVALID_USER.language = "a language less likely than even esperanto";
        INVALID_USER.nickname = "a nickname like steviest green";
        INVALID_USER.postcode = "a code that doesn't exist in the known universe";
        INVALID_USER.randStr = "a string that the random string function won't make";
        INVALID_USER.timezone = "a timezone for GMT-24:00??";
    }
}
