/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.web.sxsw;

public interface SimilarityEngine {
    public double getDistance(String key1, String key2);
}