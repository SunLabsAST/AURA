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

package com.sun.labs.aura.datastore.impl.store;

import com.sun.labs.aura.datastore.Attention;
import java.io.Serializable;
import java.util.Comparator;

/**
 * Compares attention objects to sort in reverse chronological order
 */
public class ReverseAttentionTimeComparator implements Comparator<Attention>, Serializable {
    public ReverseAttentionTimeComparator() {}
    
    public int compare(Attention o1, Attention o2) {
        if(o1.getTimeStamp() - o2.getTimeStamp() < 0) {
            return 1;
        } else if(o1.getTimeStamp() == o2.getTimeStamp()) {
            return 0;
        } else {
            return -1;
        }

    }
}
