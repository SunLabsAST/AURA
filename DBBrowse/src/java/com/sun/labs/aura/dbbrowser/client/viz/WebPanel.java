
package com.sun.labs.aura.dbbrowser.client.viz;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;

/**
 * A panel that displays info about a web container
 */
public class WebPanel extends FlowPanel {
    protected static NumberFormat cpuFormat = NumberFormat.getFormat("###0.#");
    protected Panel cpuLoad = null;
    protected StyleLabel numSessions;

    public WebPanel(String procName) {
        super();
        setStylePrimaryName("viz-webPanel");
        add(new Label(procName));
        numSessions = new StyleLabel("", "viz-statLabel");
        setActiveSessions(0);
        add(numSessions);
        cpuLoad = Util.getHisto("CPU", 0, 100, 50, "0%");
        add(cpuLoad);
    }

    public void setCPULoad(double load) {
        String str = cpuFormat.format(load);
        Panel newLoad = Util.getHisto("CPU", Double.valueOf(load).intValue(), 100, 50, str + "%");
        int index = getWidgetIndex(cpuLoad);
        remove(index);
        cpuLoad = newLoad;
        insert(cpuLoad, index);
    }

    public void setActiveSessions(int numActive) {
        numSessions.setText("Active Sessions: " + numActive);
    }
}
