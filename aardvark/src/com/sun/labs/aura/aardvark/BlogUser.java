/*
 *  Copyright 2007 Sun Microsystems, Inc. 
 *  All Rights Reserved. Use is subject to license terms.
 * 
 *  See the file "license.terms" for information on usage and
 *  redistribution of this file, and for a DISCLAIMER OF ALL
 *  WARRANTIES..
 */

package com.sun.labs.aura.aardvark;

import com.sun.labs.aura.datastore.StoreFactory;
import com.sun.labs.aura.datastore.User;

/**
 *
 * @author plamere
 */
public class BlogUser extends ItemAdapter {
    private final static String FIELD_EMAIL_ADDRESS = "email";
    
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
}
