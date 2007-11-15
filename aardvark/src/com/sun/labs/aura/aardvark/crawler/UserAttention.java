/*
 *  Copyright 2007 Sun Microsystems, Inc. 
 *  All Rights Reserved. Use is subject to license terms.
 * 
 *  See the file "license.terms" for information on usage and
 *  redistribution of this file, and for a DISCLAIMER OF ALL
 *  WARRANTIES. 
 */

package com.sun.labs.aura.aardvark.crawler;

import com.sun.labs.aura.aardvark.store.Attention;
import com.sun.labs.aura.aardvark.store.item.User;
import java.lang.reflect.Type;

/**
 *
 * @author plamere
 */
public class UserAttention {
    private User user;
    private Attention.Type type;

    public UserAttention(User user, Attention.Type type) {
        this.user = user;
        this.type = type;
    }

    public Attention.Type getType() {
        return type;
    }

    public User getUser() {
        return user;
    }
}
