/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music;

import com.sun.labs.aura.recommender.ResultsFilterAdapter;
import com.sun.labs.minion.ResultAccessor;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author plamere
 */
public class StickyTagResultsFilter extends ResultsFilterAdapter {
    private Set<String> stickySet;

    public StickyTagResultsFilter(Set<String> stickyStrings) {
        stickySet = new HashSet<String>();
        for (String s : stickyStrings) {
            stickySet.add(s.toLowerCase());
        }
    }

    @Override
    protected boolean lowLevelFilter(ResultAccessor ra) {
        if (stickySet.size() > 0) {
            List list = ra.getField(Artist.FIELD_SOCIAL_TAGS);
            if (list != null) {
                List<String> tags = (List<String>) list;
                int matchCount = 0;

                for (String tag : tags) {
                    if (stickySet.contains(tag.toLowerCase())) {
                        if (++matchCount >= stickySet.size()) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
        return true;
    }
}
