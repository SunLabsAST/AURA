/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.web.sxsw;

import com.sun.labs.minion.ResultAccessor;

/**
 *
 * @author plamere
 */
public class TypeFilter extends ResultsFilterAdapter {
    private String type;

    public TypeFilter(String type) {
        this.type = type;
    }

    public boolean filterCheck(ResultAccessor ra) {
        String rt = (String) ra.getSingleFieldValue("type");
        boolean res = rt != null && rt.equals(type);
        //System.out.printf("%s vs %s %s\n", rt, type, res ? "t" : "f");
        return res;
    }
}
