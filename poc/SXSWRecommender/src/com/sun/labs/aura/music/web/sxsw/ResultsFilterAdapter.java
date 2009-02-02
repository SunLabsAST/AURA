/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.web.sxsw;

import com.sun.labs.minion.ResultAccessor;
import com.sun.labs.minion.ResultsFilter;
import java.io.Serializable;

/**
 *
 * @author plamere
 */
public abstract class ResultsFilterAdapter implements ResultsFilter, Serializable {
    private int nt;
    private int np;

    public abstract boolean filterCheck(ResultAccessor ra);

    public boolean filter(ResultAccessor ra) {
        nt++;
        boolean ret = filterCheck(ra);
        if(ret) {
            np++;
        }
        return ret;
    }

    public int getTested() {
        return nt;
    }

    public int getPassed() {
        return np;
    }
}
