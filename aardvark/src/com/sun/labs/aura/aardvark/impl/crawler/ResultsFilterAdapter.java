/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.impl.crawler;

import com.sun.kt.search.ResultAccessor;
import com.sun.kt.search.ResultsFilter;

/**
 *
 * @author plamere
 */
public abstract class ResultsFilterAdapter implements ResultsFilter {
    private int numTested;
    private int numPassed;

    abstract protected boolean lowLevelFilter(ResultAccessor ra);

    public boolean filter(ResultAccessor ra) {
        numTested++;
        boolean result = lowLevelFilter(ra);
        if (result) {
            numPassed++;
        }
        return result;
    }

    public int getTested() {
        return numTested;
    }

    public int getPassed() {
        return numPassed;
    }
}
