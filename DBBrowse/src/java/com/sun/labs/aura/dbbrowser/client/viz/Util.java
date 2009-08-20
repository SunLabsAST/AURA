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

import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility methods for the viz panel.
 * @author jhalex
 */
public class Util {
    
    protected static HashMap<String,String> codeToDisplay;
    
    static {
        codeToDisplay = new HashMap<String,String>();
        codeToDisplay.put("ATTEND",          "Attentions");
        codeToDisplay.put("EXPLAIN_SIM",     "Explain sim");
        codeToDisplay.put("FIND_SIM",        "Find similars");
        codeToDisplay.put("FIND_SIM_AUTOTAGS","Find sim autotags");
        codeToDisplay.put("GET_ALL",         "Get all items");
        codeToDisplay.put("GET_ALL_ITR",     "Get all item iter");
        codeToDisplay.put("GET_ATTN",        "Get attention");
        codeToDisplay.put("GET_ATTN_CNT",    "Get attention cnt");
        codeToDisplay.put("GET_ATTN_ITR",    "Get attention iter");
        codeToDisplay.put("GET_ATTN_SINCE",  "Get attention since");
        codeToDisplay.put("GET_ATTN_SINCE_CNT","Get attn cnt since");
        codeToDisplay.put("GET_ATTN_SINCE_ITR","Get attn itr since");
        codeToDisplay.put("GET_AUTOTAGGED",  "Get autotagged");
        codeToDisplay.put("GET_DV_KEY",      "Get DV for key");
        codeToDisplay.put("GET_DV_CLOUD",    "Get DV for cloud");
        codeToDisplay.put("GET_EXPLAIN",     "Get explanation");
        codeToDisplay.put("GET_ITEM",        "Get item");
        codeToDisplay.put("GET_ITEMS",       "Get items");
        codeToDisplay.put("GET_ITEMS_SINCE", "Get items since");
        codeToDisplay.put("GET_LAST_ATTN",   "Get last attention");
        codeToDisplay.put("GET_SCORED_ITEMS","Get scored items");
        codeToDisplay.put("GET_TOP_AUTOTAG_TERMS","Get top autotag terms");
        codeToDisplay.put("GET_TOP_TERMS",   "Get top terms");
        codeToDisplay.put("GET_TOP_TERM_COUNTS",   "Get top term counts");
        codeToDisplay.put("GET_TOP_VALUES",  "Get top values");
        codeToDisplay.put("GET_USER",        "Get user");
        codeToDisplay.put("NEW_ITEM",        "New item");
        codeToDisplay.put("PUT_ITEM",        "Put item");
        codeToDisplay.put("UPDATE_ITEM",     "Update item");
        codeToDisplay.put("QUERY",           "Query");
        codeToDisplay.put("PROCESS_ATTN",    "Process Attention");
    }
    
    public static String logCodeToDisplay(String code) {
        return codeToDisplay.get(code);
    }
    
    public static String[] getStatDisplayCodes() {
        String val = Cookies.getCookie("STAT_CODES");
        if (val == null) {
            ArrayList<String> defaults = new ArrayList<String>();
            defaults.add("ATTEND");
            defaults.add("NEW_ITEM");
            defaults.add("UPDATE_ITEM");
            defaults.add("GET_ITEM");
            defaults.add("FIND_SIM");
            setStatDisplayCodes(defaults);
            return getStatDisplayCodes();
        }
        
        return val.split(":");
    }
    
    public static void setStatDisplayCodes(List<String> codes) {
        StringBuffer sb = new StringBuffer();
        for (Iterator<String> it = codes.iterator(); it.hasNext();) {
            sb.append(it.next());
            if (it.hasNext()) {
                sb.append(":");
            }
        }
        Date exp = new Date();
        long aLongTime = (1000 * 60 * 60 * 24 * 365 * 10); // roughly 10 years
        Cookies.setCookie("STAT_CODES", sb.toString(), new Date(exp.getTime() + aLongTime));
    }
    

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

    public static HorizontalPanel getHisto(String name, int value, int maxValue, int maxWidth, String text) {

        int leftWidth = Math.round(((float)value / (float)maxValue) * (float)maxWidth);
        if (leftWidth < 1) {
            leftWidth = 1;
        } else if (leftWidth > maxWidth) {
            leftWidth = maxWidth;
        }
        int rightWidth = maxWidth - leftWidth;
        boolean alert = false;
        if (value / (float)maxValue > 0.75) {
            alert = true;
        }

        HorizontalPanel all = new HorizontalPanel();
        all.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
        all.add(new StyleLabel(name + ":", "viz-histoName"));

        HorizontalPanel table = new HorizontalPanel();
        table.setWidth(maxWidth+"px");
        table.setBorderWidth(0);
        table.setSpacing(0);

        Widget left = new Label("");
        if (alert) {
            left.setStyleName("viz-histoLeftAlert");
        } else {
            left.setStyleName("viz-histoLeft");
        }
        left.setWidth(leftWidth + "");
        left.setHeight("10px");
        left.getElement().getStyle().setPropertyPx("fontSize", 6);

        Widget right = new Label("");
        if (alert) {
            right.setStyleName("viz-histoRightAlert");
        } else {
            right.setStyleName("viz-histoRight");
        }
        right.setWidth(rightWidth + "");
        right.setHeight("10px");
        right.getElement().getStyle().setPropertyPx("fontSize", 6);

        table.add(left);
        table.add(right);
        all.add(table);
        all.add(new StyleLabel(text, "viz-histoText"));
        return all;
    }
}
