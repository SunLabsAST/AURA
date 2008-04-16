/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.impl.recommender;

import com.sun.kt.search.ResultAccessor;
import com.sun.kt.search.ResultsFilter;
import com.sun.labs.aura.aardvark.BlogEntry;
import java.io.Serializable;
import java.util.Date;

/**
 * A results filter that will exclude keys based upon their age
 */
public class DateExclusionFilter implements ResultsFilter, Serializable {
    private Date earliest;
    private int in;
    private int out;
    
    /**
     * creates a new date exclusion filter
     * @param earliestDate the earliest allowed date
     */
    public DateExclusionFilter(Date earliestDate) { 
        this.earliest = earliestDate;
    }

    /**
     * Filter a result based upon the date. If an item occurred before the
     * earliest date, or if the item has no timestamp at all, we don't include it.
     * @param ra the accessor for the result
     * @return true only if the result as a field with a date that is after earliest
     */
    public boolean filter(ResultAccessor ra) {
        Date date = (Date) ra.getSingleFieldValue(BlogEntry.FIELD_PUBLISH_DATE);
        boolean result =  date != null && earliest.before(date);
        if (result) {
            in++;
        } else {
            out++;
        }
        return result;
    }
    
    public int getTested() {
        return in + out;
    }
    
    public int getPassed() {
        return in;
    }
    
    public String toString() {
        return "in " + in + " out " + out;
    }
}
