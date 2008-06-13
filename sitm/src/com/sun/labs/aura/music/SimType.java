/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */

package com.sun.labs.aura.music;

import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import java.util.List;

/**
 *
 * @author plamere
 */
public interface SimType {
    public String getDescription();
    public String getName();
    public List<Scored<Artist>> findSimilarArtists(String artistID, int count) throws AuraException;
}
