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

import com.google.gwt.user.client.rpc.IsSerializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author mailletf
 */
public class AttentionItem<T extends IsSerializable> implements IsSerializable {

    private T item;
    private int rating;
    private Date date;
    private Set<String> tags;

    public AttentionItem() {

    }

    public AttentionItem(T item) {
        this.item = item;
        this.rating = 0;
        this.tags = new HashSet<String>();
    }

    public void setItem(T item) {
        this.item = item;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public int getRating() {
        return rating;
    }

    public Set<String> getTags() {
        return tags;
    }

    public T getItem() {
        return item;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(long timestamp) {
        this.date = new Date(timestamp);
    }

    public void setDate(Date d) {
        this.date = d;
    }

}
