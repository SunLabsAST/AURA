/*
 *  Copyright 2007 Sun Microsystems, Inc. 
 *  All Rights Reserved. Use is subject to license terms.
 * 
 *  See the file "license.terms" for information on usage and
 *  redistribution of this file, and for a DISCLAIMER OF ALL
 *  WARRANTIES. 
 */

package com.sun.labs.aura.aardvark.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 *
 * @author plamere
 */
public class WiUserStatus implements IsSerializable {
    private String status;
    private WiUser user;

    public WiUserStatus() {

    }

    public WiUserStatus(String status, WiUser user) {
        this.status = status;
        this.user = user;
    }

    public String getStatus() {
        return status;
    }

    public WiUser getUser() {
        return user;
    }
}
