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

package com.sun.labs.aura.aardvark.impl.recommender;

import com.sun.labs.aura.aardvark.BlogEntry;
import com.sun.labs.minion.ResultAccessor;
import com.sun.labs.minion.ResultsFilter;
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
