/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music;

import com.sun.labs.aura.recommender.ResultsFilterAdapter;
import com.sun.labs.minion.ResultAccessor;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author plamere
 */
public class KeyResultsFilter extends ResultsFilterAdapter {
	private Set<String> keys;

	KeyResultsFilter() {
		keys = new HashSet<String>();
	}

	KeyResultsFilter(Set<String>keys) {
		this.keys = keys;
	}

	@Override
	protected boolean lowLevelFilter(ResultAccessor ra) {
		return !keys.contains(ra.getKey());
	}
}
