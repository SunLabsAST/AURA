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

package com.sun.labs.aura.music.crawler;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author mailletf
 */
public class ConcurrentCrawler {

    private final Set<String> crawlsInProgress = new HashSet<String>();
    
    /**
     * Remove item from crawl set when we're done crawling it
     * @param uid id of item
     */
    protected void removeFromCrawlList(String uid) {
        synchronized (crawlsInProgress) {
            crawlsInProgress.remove(uid);
        }
    }

    /**
     * Add item id to crawl set to make sure two crawler theads don't start crawling
     * it at the same time.
     * @param uid id of item
     */
    protected boolean addToCrawlList(String uid) {
        synchronized (crawlsInProgress) {
            if (crawlsInProgress.contains(uid)) {
                return false;
            } else {
                crawlsInProgress.add(uid);
                return true;
            }
        }
    }
    
}
