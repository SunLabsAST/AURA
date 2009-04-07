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

package com.sun.labs.aura.recommender;

import com.sun.labs.minion.ResultAccessor;
import com.sun.labs.minion.ResultsFilter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author plamere
 */
public class RecommenderProfile extends ResultsFilterAdapter implements Serializable {

    private List<ResultsFilter> resultsFilterList;

    public RecommenderProfile() {
        resultsFilterList = new ArrayList<ResultsFilter>();
    }

    public void addResultFilter(ResultsFilter resultsFilter) {
        resultsFilterList.add(resultsFilter);
    }

    protected boolean lowLevelFilter(ResultAccessor ra) {
        for (ResultsFilter rf : resultsFilterList) {
            if (rf.filter(ra)) {
                return false;
            }
        }
        return true;
    }

    public static ResultsFilter getLengthConstraint(final String field, final int length) {
        return new ResultsFilterAdapter() {

            public boolean lowLevelFilter(ResultAccessor ra) {
                String v = (String) ra.getSingleFieldValue(field);
                return v != null && v.length() >= length;
            }
        };
    }

    public static ResultsFilter getDateConstraint(final String field, final Date earliest) {
        return new ResultsFilterAdapter() {

            public boolean lowLevelFilter(ResultAccessor ra) {
                Date date = (Date) ra.getSingleFieldValue(field);
                boolean result = date != null && earliest.before(date);
                return result;
            }
        };
    }
}
