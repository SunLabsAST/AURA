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

package com.sun.labs.aura.cluster;

import com.sun.labs.aura.datastore.Item;
import com.sun.labs.minion.DocumentVector;
import com.sun.labs.minion.clustering.ClusterUtil;
import java.io.Serializable;
import java.util.Arrays;

/**
 * A single element in a results cluster.
 *
 * @author Stephen Green <stephen.green@sun.com>
 */
public class ClusterElement implements Comparable<ClusterElement>, Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * The point in N-space representing this element.
     */
    public double[] point;
    
    /**
     * The actual item that this element represents.
     */
    protected Item item;
    
    transient protected DocumentVector dv;
    
    /**
     * The cluster that this element is a member of.
     */
    protected int member;
    
    protected double dist;
    
    /**
     * Creates a ClusterElement
     */
    public ClusterElement(Item item, DocumentVector dv) {
        this.item = item;
        this.dv = dv;
        this.member = -1;
    }
    
    protected void setPoint(double[] point) {
        this.point = point;
    }
    
    public double[] getPoint() {
        if (point != null) {
            return Arrays.copyOf(point, point.length);
        } else {
            return null;
        }
    }
    
    /**
     * Computes the distance between this cluster element an a given point in
     * the same space.
     */
    public double dist(double[] p) {
        dist = ClusterUtil.euclideanDistance(point, p);
        return dist;
    }
    
    public double getDistance() {
        return dist;
    }
    
    public String toString() {
        return item.getKey();
    }
    
    public Item getItem() {
        return item;
    }
    
    /**
     * Returns true if the elements have the same item and position
     */
    public boolean equals(Object o) {
        if (o instanceof ClusterElement) {
            ClusterElement oc = (ClusterElement)o;
            if (oc.getItem().equals(item) && Arrays.equals(point, oc.point)) {
                return true;
            }
        }
        return false;
    }
    
    public int hashCode() {
        return item.hashCode();
    }
    
    public int compareTo(ClusterElement o) {
        if(dist < o.dist) {
            return -1;
        }
        if(dist > o.dist) {
            return 1;
        }
        return 0;
    }
}
