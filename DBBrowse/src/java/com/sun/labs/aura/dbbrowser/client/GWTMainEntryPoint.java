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

package com.sun.labs.aura.dbbrowser.client;

import com.sun.labs.aura.dbbrowser.client.query.DBServiceAsync;
import com.sun.labs.aura.dbbrowser.client.query.DBService;
import com.sun.labs.aura.dbbrowser.client.query.TabbedQueryUI;
import com.sun.labs.aura.dbbrowser.client.viz.VizUI;
import com.sun.labs.aura.dbbrowser.client.viz.VizServiceAsync;
import com.sun.labs.aura.dbbrowser.client.viz.VizService;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * The entry point that sets up the initial page
 */
public class GWTMainEntryPoint implements EntryPoint {
    
    /** Creates a new instance of GWTMainEntryPoint */
    public GWTMainEntryPoint() {
    }
    
    /**
     * The entry point method, called automatically by loading a module
     * that declares an implementing class as an entry-point
     */
    public void onModuleLoad() {
        MainSelector sel = new MainSelector();
        sel.addTool("Query DB", new TabbedQueryUI());
        VizUI vizui = new VizUI();
        sel.addTool("Viz UI", vizui);
        RootPanel.get().add(sel);
        sel.select(vizui);
    }
    
    public static DBServiceAsync getDBService(){
        // Create the client proxy. Note that although you are creating the
        // service interface proper, you cast the result to the asynchronous
        // version of
        // the interface. The cast is always safe because the generated proxy
        // implements the asynchronous interface automatically.
        DBServiceAsync service = (DBServiceAsync) GWT.create(DBService.class);
        // Specify the URL at which our service implementation is running.
        // Note that the target URL must reside on the same domain and port from
        // which the host page was served.
        //
        ServiceDefTarget endpoint = (ServiceDefTarget) service;
        String moduleRelativeURL = GWT.getModuleBaseURL() + "dbservice";
        endpoint.setServiceEntryPoint(moduleRelativeURL);
        return service;
    }

    public static VizServiceAsync getVizService(){
        // Create the client proxy. Note that although you are creating the
        // service interface proper, you cast the result to the asynchronous
        // version of
        // the interface. The cast is always safe because the generated proxy
        // implements the asynchronous interface automatically.
        VizServiceAsync service = (VizServiceAsync) GWT.create(VizService.class);
        // Specify the URL at which our service implementation is running.
        // Note that the target URL must reside on the same domain and port from
        // which the host page was served.
        //
        ServiceDefTarget endpoint = (ServiceDefTarget) service;
        String moduleRelativeURL = GWT.getModuleBaseURL() + "vizservice";
        endpoint.setServiceEntryPoint(moduleRelativeURL);
        return service;
    }

}
