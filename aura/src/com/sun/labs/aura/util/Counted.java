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
package com.sun.labs.aura.util;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Items that have a count associated with them.
 * @param <T> The type of item that is counted.
 */
public class Counted<T extends Comparable<T>> implements Serializable {

    private T item;

    private int count;

    /**
     * Creates a scored item
     * @param item the item
     * @param count the count
     */
    public Counted(T item, int count) {
        this.item = item;
        this.count = count;
    }

    /**
     * Gets the item
     * @return the item
     */
    public T getItem() {
        return item;
    }

    public String toString() {
        return String.format("<%d, %s>", count, item.toString());
    }

    /**
     * Gets the score of the item
     * @return  the score for the scored item
     */
    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean equals(Object o) {
        if(o == null) {
            return false;
        }

        if(this == o) {
            return true;
        }

        if(!(o instanceof Counted)) {
            return false;
        }
        Counted c = (Counted) o;
        return item.equals(c.item) && count == c.count;
    }

    public int hashCode() {
        return item.hashCode();
    }

    public int compareTo(Counted<T> o) {
        int cmp = ((Comparable) item).compareTo(o);
        if(cmp == 0) {
            if(count < o.count) {
                return -1;
            } else if(count > o.count) {
                return 1;
            }
            return 0;
        }
        return cmp;
    }
    public static final Comparator<Counted> COUNT_COMPARATOR = new Comparator<Counted>() {

        @Override
        public int compare(Counted o1, Counted o2) {
            return o1.count - o2.count;
        }
    };

    public static final Comparator<Counted> INV_COUNT_COMPARATOR = new Comparator<Counted>() {

        @Override
        public int compare(Counted o1, Counted o2) {
            return o2.count - o1.count;
        }
    };

}
