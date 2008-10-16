/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.dbbrowser.client.viz;

import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author jalex
 */
public class Util {

    public static DisclosurePanel getTypeStatsPanel(Map typeToCount) {
        DisclosurePanel typeStats = new DisclosurePanel(
                new StyleLabel("Item Type Stats", "viz-typeStatsLabel"));
        FlexTable typeTable = new FlexTable();
        CellFormatter cf = typeTable.getCellFormatter();
        Set types = typeToCount.keySet();
        int row=0;
        for (Iterator typeIt = types.iterator(); typeIt.hasNext();) {
            String type = (String)typeIt.next();
            Long count = (Long) typeToCount.get(type);
            if (count.longValue() != 0L) {
                typeTable.setWidget(row, 0,
                        new StyleLabel(type + ":","viz-statLabel"));
                typeTable.setWidget(row, 1,
                        new StyleLabel(count.toString(), "viz-statLabel"));
                cf.setStylePrimaryName(row, 0, "viz-typeStatsTypeCol");
                cf.setStylePrimaryName(row++, 1, "viz-typeStatsCountCol");
            }
        }
        typeStats.add(typeTable);
        typeStats.setStylePrimaryName("viz-typeStatsPanel");
        return typeStats;
    }
}
