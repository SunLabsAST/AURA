
package com.sun.labs.aura.datastore;

import java.util.Date;

/**
 * Represents a user in the system.  A user is a specialized type of item.
 */
public interface User extends Item {

    /**
     * Gets the random string that is used in URLs to this user's data
     *
     * @return the random string
     */
    public String getUserRandString();

    /**
     * @return the dob
     */
    public Date getDob();

    /**
     * @return the email
     */
    public String getEmail();

    /**
     * @return the fullname
     */
    public String getFullname();

    /**
     * @return the gender
     */
    public String getGender();

    /**
     * @return the language
     */
    public String getLanguage();

    /**
     * @return the nickname
     */
    public String getNickname();

    /**
     * @return the postcode
     */
    public String getPostcode();

    /**
     * @return the country
     */
    public String getCountry();

    /**
     * @return the timezone
     */
    public String getTimezone();

    /**
     * @param dob the dob to set
     */
    public void setDob(Date dob);

    /**
     * @param email the email to set
     */
    public void setEmail(String email);

    /**
     * @param fullname the fullname to set
     */
    public void setFullname(String fullname);

    /**
     * @param gender the gender to set
     */
    public void setGender(String gender);

    /**
     * @param language the language to set
     */
    public void setLanguage(String language);

    /**
     * @param nickname the nickname to set
     */
    public void setNickname(String nickname);

    /**
     * @param postcode the postcode to set
     */
    public void setPostcode(String postcode);

    /**
     * @param country the country to set
     */
    public void setCountry(String country);

    /**
     * @param timezone the timezone to set
     */
    public void setTimezone(String timezone);
}
