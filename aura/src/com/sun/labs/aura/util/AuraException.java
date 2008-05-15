/*
 * AuraException.java
 * 
 * Created on Oct 25, 2007, 4:02:26 PM
 * 
 */

package com.sun.labs.aura.util;

/**
 * An exception thrown by the Aura system.
 * 
 * @author ja151348
 */
public class AuraException extends Exception {

    /**
     * Creates an exception with a specific text message
     * @param message the detailed message associated with the exception
     */
    public AuraException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a specfic text message and cause.
     * @param message the detailed message
     * @param cause the root cause of the exception
     */
    public AuraException(String message, Throwable cause) {
        super(message, cause);
    }
}
