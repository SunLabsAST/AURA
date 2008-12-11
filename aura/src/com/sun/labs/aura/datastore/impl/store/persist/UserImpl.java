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
@Persistent(version = 3)
public class UserImpl extends ItemImpl implements User {
    private static final long serialVersionUID = 3;

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
}
