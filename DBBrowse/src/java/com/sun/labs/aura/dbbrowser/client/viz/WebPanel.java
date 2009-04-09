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

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;

/**
 * A panel that displays info about a web container
 */
public class WebPanel extends FlowPanel implements Comparable {
    protected Panel cpuLoad = null;
    protected int numActive = 0;
    protected StyleLabel numSessions;
    protected String procName;

    public WebPanel(String procName) {
        super();
        this.procName = procName;
        setStylePrimaryName("viz-webPanel");
        add(new Label(procName));
        numSessions = new StyleLabel("", "viz-statLabel");
        setActiveSessions(0);
        add(numSessions);
        cpuLoad = Util.getHisto("CPU", 0, 100, 50, "0%");
        add(cpuLoad);
    }

    public void setCPULoad(double load) {
        String str = VizUI.cpuFormat.format(load);
        Panel newLoad = Util.getHisto("CPU", Double.valueOf(load).intValue(), 100, 50, str + "%");
        int index = getWidgetIndex(cpuLoad);
        remove(index);
        cpuLoad = newLoad;
        insert(cpuLoad, index);
    }

    public void setActiveSessions(int numActive) {
        this.numActive = numActive;
        numSessions.setText("Active Sessions: " + numActive);
    }

    public int getActiveSessions() {
        return numActive;
    }

    public int compareTo(Object o) {
        if (o instanceof WebPanel) {
            return procName.compareTo(((WebPanel)o).procName);
        }
        return 1;
    }

    /**
     * @return the procName
     */
    public String getProcName() {
        return procName;
    }

    /**
     * @param procName the procName to set
     */
    public void setProcName(String procName) {
        this.procName = procName;
    }
}
