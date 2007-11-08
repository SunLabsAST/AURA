/*
 *  Copyright 2007 Sun Microsystems, Inc. 
 *  All Rights Reserved. Use is subject to license terms.
 * 
 *  See the file "license.terms" for information on usage and
 *  redistribution of this file, and for a DISCLAIMER OF ALL
 *  WARRANTIES. 
 */
package com.sun.labs.aura.aardvark.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

/**
 * Factory that returns a singleton AardvarkService
 */
public class AardvarkServiceFactory {

    private static AardvarkServiceAsync service = null;

    public static AardvarkServiceAsync getService() {

        if (service == null) {
            service = (AardvarkServiceAsync) GWT.create(AardvarkService.class);
            ServiceDefTarget endpoint = (ServiceDefTarget) service;
            String moduleRelativeURL = GWT.getModuleBaseURL() + "aardvarkservice";
            endpoint.setServiceEntryPoint(moduleRelativeURL);
        }
        return service;
    }
}
