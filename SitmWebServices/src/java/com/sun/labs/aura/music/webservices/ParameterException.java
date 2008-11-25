/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */

package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.music.webservices.Util.ErrorCode;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author plamere
 */
public class ParameterException extends Exception {
    private List<ErrorDescription> errors = new ArrayList<ErrorDescription>();

    ParameterException() {
    }

    ParameterException(ErrorCode errorCode, String message) {
        addError(errorCode, message);
    }

    void addError(ErrorCode errorCode, String message) {
        errors.add(new ErrorDescription(errorCode, message));
    }

    List<ErrorDescription> getErrors() {
        return errors;
    }
}
