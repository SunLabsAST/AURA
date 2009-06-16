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

package com.sun.labs.aura.music.web.lastfm;

import java.io.IOException;

/**
 *
 * @author mailletf
 */
public interface LastFM {

    /**
     * @deprecated
     */
    public SocialTag[] getArtistTags(String artistName) throws IOException;

    /**
     * @deprecated
     */
    public SocialTag[] getAlbumTags(String artistName, String trackName) throws IOException;

    /**
     * @deprecated
     */
    public SocialTag[] getTrackTags(String artistName, String trackName) throws IOException;

    /**
     * @deprecated
     */
    public SocialTag[] getArtistTags(String artistName, boolean raw) throws IOException;

    /**
     * @deprecated
     */
    public LastItem[] getArtistFans(String artistName) throws IOException;

    /**
     * @deprecated
     */
    public LastUser getUser(String userName) throws IOException;

    /**
     * @deprecated
     */
    public void setMinimumCommandPeriod(long period) throws IOException;

    /**
     * @deprecated
     */
    public int getPopularity(String artistName) throws IOException;

    /**
     * @deprecated
     */
    public LastItem[] getTopArtistsForUser(String user) throws IOException;

    /**
     * @deprecated
     */
    public LastItem[] getWeeklyArtistsForUser(String user) throws IOException;

    /**
     * @deprecated
     */
    public String[] getSimilarUsers(String user) throws IOException;

    /**
     * @deprecated
     */
    public LastArtist[] getSimilarArtists(String artist) throws IOException;

    /**
     * @deprecated
     */
    public void setTrace(boolean trace) throws IOException;

    /**
     * @deprecated
     */
    public LastUser getFanFromLastFM(String url) throws IOException;

    /**
     * @deprecated
     */
    public LastItem[] getTopArtistsForTag(String tag) throws IOException;

}
