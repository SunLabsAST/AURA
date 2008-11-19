/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.dbbrowser.client.viz;

import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author jalex
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
        codeToDisplay.put("GET_TOP_VALUES",  "Get top values");
        codeToDisplay.put("GET_USER",        "Get user");
        codeToDisplay.put("NEW_ITEM",        "New item");
        codeToDisplay.put("PUT_ITEM",        "Put item");
        codeToDisplay.put("UPDATE_ITEM",     "Update item");
        codeToDisplay.put("QUERY",           "Query");
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
}
