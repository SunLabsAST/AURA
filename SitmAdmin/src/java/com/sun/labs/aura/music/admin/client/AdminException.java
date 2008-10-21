/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.admin.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 *
 * @author plamere
 */
public class AdminException extends Exception implements IsSerializable {

    private String displayMessage;

    public AdminException() {
    }

    public AdminException(String message) {
        super(message);
    }

    public AdminException(String message, String displayMessage) {
        super(message);
        this.displayMessage = displayMessage;
    }

    public String getDisplayMessage() {
        return displayMessage;
    }
}
