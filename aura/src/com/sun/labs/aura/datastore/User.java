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
