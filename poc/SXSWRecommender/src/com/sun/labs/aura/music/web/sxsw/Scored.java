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

package com.sun.labs.aura.music.web.sxsw;


import java.io.Serializable;
import java.util.Arrays;

/**
 * Items that have a score associated with them, as well as additional sorting
 * criteria.
 * 
 * @param T the type of the scored item.
 */
//public class Scored<T> implements Comparable<Scored<T>>, Serializable {
public class Scored<T> implements Serializable {
    private T item;
    private double score;

    /**
     * Creates a scored item
     * @param item the item
     * @param score the score
     */
    public Scored(T item, double score) {
        this.item = item;
        this.score = score;
    }

    /**
     * Gets the item
     * @return the item
     */
    public T getItem() {
        return item;
    }
    
    @Override
    public String toString() {
        return String.format("<%.3f, %s>", score, item.toString());
    }

    /**
     * Gets the score of the item
     * @return  the score for the scored item
     */
    public double getScore() {
        return score;
    }
}
