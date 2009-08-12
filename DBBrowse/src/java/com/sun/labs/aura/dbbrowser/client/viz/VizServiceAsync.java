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

package com.sun.labs.aura.dbbrowser.client.viz;
import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.List;


/**
 * The async interface for the Viz service
 */
public interface VizServiceAsync {
    public void dump(AsyncCallback callback);
    
    public void refreshSvcs(AsyncCallback asyncCallback);

    public void getDSHInfo(AsyncCallback asyncCallback);

    public void getPCInfo(AsyncCallback asyncCallback);
    
    public void getRepStats(String prefix, AsyncCallback asyncCallback);
    
    public void resetRepStats(String prefix, AsyncCallback asyncCallback);

    public void getCPULoads(AsyncCallback asyncCallback);

    public void getWebStats(AsyncCallback asyncCallback);

    public void getRepLogNames(AsyncCallback asyncCallback);

    public void getRepSelectedLogNames(String prefix, AsyncCallback asyncCallback);

    public void setRepSelectedLogNames(String prefix, List<String> selected, AsyncCallback asyncCallback);

    public void getLogLevel(String prefix, AsyncCallback asyncCallback);

    public void setLogLevel(String prefix, String level, AsyncCallback asyncCallback);

    public void haltPC(PCInfo pc, AsyncCallback asyncCallback);
    
    public void splitPC(PCInfo pc, AsyncCallback asyncCallback);
    
    public void shutDown(AsyncCallback asyncCallback);
}
