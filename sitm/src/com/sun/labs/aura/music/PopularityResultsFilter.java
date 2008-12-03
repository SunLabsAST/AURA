/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music;

import com.sun.labs.minion.ResultAccessor;
import com.sun.labs.aura.music.MusicDatabase.Popularity;
import com.sun.labs.aura.recommender.ResultsFilterAdapter;

/**
 *
 * @author plamere
 */
public class PopularityResultsFilter extends ResultsFilterAdapter {

	private float maxPopularity;
	private float minPopularity;
	private boolean needsPopFilter = false;
	public final static float POPULARITY_SHORT = 1.00f;
	public final static float POPULARITY_MEDIUM = 0.05f;
	public final static float POPULARITY_LONG = 0.001f;

	public PopularityResultsFilter(Popularity popularity, float maxUnnormalizedPopularity) {
		if (popularity != Popularity.ALL) {
			needsPopFilter = true;
			this.maxPopularity = getMaxPopularity(popularity) * maxUnnormalizedPopularity;
			this.minPopularity = getMinPopularity(popularity) * maxUnnormalizedPopularity;
		}
	}

	@Override
	protected boolean lowLevelFilter(ResultAccessor ra) {
		boolean res = true;
		if (needsPopFilter) {
			Object obj = ra.getSingleFieldValue(Artist.FIELD_POPULARITY);
			if (obj != null && obj instanceof Number) {
				Number n = (Number) obj;
				float pop = n.floatValue();
				res = pop <= maxPopularity && pop >= minPopularity;
			}
		}
		return res;
	}

	private float getMaxPopularity(Popularity pop) {
		float max = PopularityResultsFilter.POPULARITY_SHORT;

		switch (pop) {
			case ALL:
				max = 1f;
				break;
			case HEAD:
				max = 1f;
				break;
			case MID:
				max = PopularityResultsFilter.POPULARITY_MEDIUM;
				break;
			case TAIL:
				max = PopularityResultsFilter.POPULARITY_LONG;
				break;
			case HEAD_MID:
				max = 1.0f;
				break;
			case MID_TAIL:
				max = PopularityResultsFilter.POPULARITY_MEDIUM;
				break;
			default:
				max = 1;
				break;
		}

		return max;
	}

	private float getMinPopularity(Popularity pop) {
		float min = 0;
		switch (pop) {
			case ALL:
				min = 0f;
				break;
			case HEAD:
				min = PopularityResultsFilter.POPULARITY_MEDIUM;
				break;
			case MID:
				min = PopularityResultsFilter.POPULARITY_LONG;
				break;
			case TAIL:
				min = 0;
				break;
			case HEAD_MID:
				min = PopularityResultsFilter.POPULARITY_LONG;
				break;
			case MID_TAIL:
				min = 0;
				break;
			default:
				min = 0;
				break;

		}
		return min;
	}
}
