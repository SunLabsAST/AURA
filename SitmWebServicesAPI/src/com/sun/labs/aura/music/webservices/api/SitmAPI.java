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

package com.sun.labs.aura.music.webservices.api;

import java.io.IOException;
import java.util.List;
import org.w3c.dom.Document;

/**
 *
 * @author ja151348
 */
public abstract class SitmAPI {

    public abstract List<Scored<Item>> artistSearch(String searchString) throws IOException;

    public abstract List<Scored<Item>> artistSocialTags(String key, int count) throws IOException;

    public abstract long checkStatus(String msg, Document doc) throws IOException;

    public abstract List<Scored<Item>> findSimilarArtistFromWordCloud(String cloud, int count) throws IOException;

    public abstract List<Scored<Item>> findSimilarArtistTags(String key, int count) throws IOException;

    public abstract List<Scored<Item>> findSimilarArtistsByKey(String key, int count) throws IOException;

    public abstract List<Scored<Item>> findSimilarArtistsByName(String name, int count) throws IOException;

    public abstract List<Item> getArtistTags(int count) throws IOException;

    public abstract List<Item> getArtists(int count) throws IOException;

    public abstract Item getItem(String key, boolean compact) throws IOException;

    public abstract List<Item> getItems(List<String> keys, boolean compact) throws IOException;

    public abstract void getStats() throws IOException;

    public abstract void resetStats();

    public abstract void showStats();

    public abstract List<Scored<Item>> tagSearch(String searchString, int count) throws IOException;

    public static SitmAPI getSitmAPI(String host, boolean trace, boolean debug, boolean periodicDump) throws IOException {
        return new SitmAPIImpl(host, trace, debug, periodicDump);
    }
}
