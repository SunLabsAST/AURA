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

package com.sun.labs.aura.music.wsitm.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 *
 * @author mailletf
 */
public class WebException extends RuntimeException implements IsSerializable {

    private errorMessages errorType;

    public static enum errorMessages {
        ITEM_STORE_COMMUNICATION_FAILED,
        RPC_STATUS_CODE_EXCEPTION,
        MUST_BE_LOGGED_IN,
        INVALID_MENU_CALLED,
        INIT_ERROR
    }

    public WebException() {
        super();
    }

    /**
     * Creates an exception with a specific text message
     * @param message the detailed message associated with the exception
     */
    public WebException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a specfic text message and cause.
     * @param message the detailed message
     * @param cause the root cause of the exception
     */
    public WebException(String message, Throwable cause) {
        super(message, cause);
    }

    public WebException(errorMessages eM) {
        super(getErrorMessage(eM));
        errorType = eM;
    }

    public WebException(errorMessages eM, Throwable cause) {
        super(getErrorMessage(eM), cause);
        errorType = eM;
    }

    private static String getErrorMessage(errorMessages eM) {

        if (eM == errorMessages.ITEM_STORE_COMMUNICATION_FAILED) {
            return "A problem occured while trying to communicate with the datastore.";
        } else if (eM == errorMessages.RPC_STATUS_CODE_EXCEPTION) {
            return "A server problem occured while processing your request.";
        } else if (eM == errorMessages.MUST_BE_LOGGED_IN) {
            return "You must be logged in to perform this action.";
        } else if (eM == errorMessages.INVALID_MENU_CALLED) {
            return "The menu that was called is invalid. Invalid class.";
        } else if (eM == errorMessages.INIT_ERROR) {
            return "Error initializing application.";
        } else {
            return "Unkonwn error";
        }

    }

    public errorMessages getErrorType() {
        return errorType;
    }

    public boolean equals(WebException eX) {
        if (errorType!=null && eX.errorType!=null && errorType==eX.getErrorType()) {
            return true;
        } else {
            return false;
        }
    }
}

