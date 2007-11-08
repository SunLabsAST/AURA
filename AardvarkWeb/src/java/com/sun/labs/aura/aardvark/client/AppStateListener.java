/*
 *  Copyright 2007 Sun Microsystems, Inc. 
 *  All Rights Reserved. Use is subject to license terms.
 * 
 *  See the file "license.terms" for information on usage and
 *  redistribution of this file, and for a DISCLAIMER OF ALL
 *  WARRANTIES. 
 */

package com.sun.labs.aura.aardvark.client;

/**
 * An interface used to allow the various interface components to communicate
 * changtes in state
 */
public interface AppStateListener {
    public void setCurrentUser(WiUser user);

    public void info(String msg);
    public void clearInfo();
    public void error(String msg);
}
