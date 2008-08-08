/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 *
 * @author mailletf
 */
public class WebException extends Exception implements IsSerializable {

    private errorMessages errorType;

    public static enum errorMessages {
        ITEM_STORE_COMMUNICATION_FAILED,
        RPC_STATUS_CODE_EXCEPTION,
        MUST_BE_LOGGED_IN,
        INVALID_MENU_CALLED
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
            return "A problem occured while trying to cummunicate with the datastore.";
        } else if (eM == errorMessages.RPC_STATUS_CODE_EXCEPTION) {
            return "A server problem occured while processing your request.";
        } else if (eM == errorMessages.MUST_BE_LOGGED_IN) {
            return "You must be logged in to perform this action.";
        } else if (eM == errorMessages.INVALID_MENU_CALLED) {
            return "The menu that was called is invalid. Invalid class.";
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

