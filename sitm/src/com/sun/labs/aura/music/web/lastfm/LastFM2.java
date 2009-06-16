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
import java.util.List;

/**
 *
 * @author mailletf
 */
public interface LastFM2 {

    public void setTrace(boolean trace) throws IOException;

    public void setMinimumCommandPeriod(long period) throws IOException;

    public LastArtist2 getArtistInfoByName(String artistName) throws IOException;

    public LastArtist2 getArtistInfoByMBID(String mbid) throws IOException;

    public LastAlbum2 getAlbumInfoByName(String artistName, String albumName) throws IOException;

    public LastAlbum2 getAlbumInfoByMBID(String mbid) throws IOException;

    public LastTrack getTrackInfoByName(String artistName, String trackName) throws IOException;

    public LastTrack getTrackInfoByMBID(String mbid) throws IOException;

    public SocialTag[] getTrackTopTagsByName(String artistName, String trackName) throws IOException;

    public SocialTag[] getTrackTopTagsByMBID(String mbid) throws IOException;

    public String[] getNeighboursForUser(String user) throws IOException;

    public List<Integer[]> getWeeklyChartListByUser(String user) throws IOException;

    public List<LastItem> getWeeklyArtistChartByUser(String user) throws IOException;

    public List<LastItem> getWeeklyArtistChartByUser(String user, int from, int to) throws IOException;
}
