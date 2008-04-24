/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.recommender;

import com.sun.kt.search.ResultAccessor;
import com.sun.kt.search.ResultsFilter;
import com.sun.labs.aura.aardvark.impl.crawler.ResultsFilterAdapter;
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
                return v != null && v.toString().length() >= length;
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
