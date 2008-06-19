/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */

package com.sun.labs.aura.music;

import com.sun.labs.aura.util.Scored;
import java.util.List;

/**
 *
 * @author plamere
 */
public interface RecommendationType {
    String getName();
    String getDescrption();
    List<Scored<Artist>> getRecommendations(Listener listener, int count, RecommendationProfile rp);
}
