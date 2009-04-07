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

package com.sun.labs.aura.music.wsitm.client.items;

/**
 *
 * @author mailletf
 */
public class ScoredTag extends ScoredC<String> {

    private boolean isSticky = false;

    public ScoredTag() {
        super();
    }

    public ScoredTag(String tag, double score, boolean isSticky) {
        super(tag, score);
        this.isSticky = isSticky;
    }

    public ScoredTag(String tag, double score) {
        super(tag, score);
    }

    public String getName() {
        return item;
    }

    public boolean isSticky() {
        return isSticky;
    }

    public void setSticky(boolean stick) {
        this.isSticky = stick;
    }

    public void setScore(double newScore) {
        this.score = newScore;
    }

    @Override
    public int hashCode() {
        return item.toLowerCase().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ScoredTag other = (ScoredTag) obj;
        if (item.toLowerCase().equals(other.item.toLowerCase())) {
            return true;
        } else {
            return false;
        }
    }
}
