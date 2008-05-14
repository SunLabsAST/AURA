/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.impl.recommender;

import com.sun.labs.minion.ResultAccessor;
import com.sun.labs.minion.ResultsFilter;
import java.io.Serializable;

/**
 *
 * @author plamere
 */
public abstract class ResultsFilterAdapter implements ResultsFilter, Serializable {
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
