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

package com.sun.labs.aura.util.io;

import java.io.IOException;
import java.util.List;

/**
 * @author Will Holcomb <william.holcomb@sun.com>
 */
public abstract class CartesianProductMerger<K, V> extends GroupedMerger {
    public void mergeList(List inputs,
                          KeyedOutputStream output) throws IOException {
        
        // The Cartesian product of an element with the empty set is the empty set
        if(inputs.size() < 2) {
            return;
        }
        // There's a n-dimensional solution, but stick to two for now
        if(inputs.size() > 2) {
            throw new IllegalArgumentException("Only two element joins currently: " + inputs.size());
        }
        
        // If I specify this as the type in the signature, the compiler doesn't
        // think the method overrides the abstract method in GroupedMerger even
        // though the type there is specified the same.
        List<List<Record<K, V>>> castInputs = (List<List<Record<K, V>>>)inputs;
        for(Record<K, V> outer : castInputs.get(0)) {
            for(Record<K, V> inner : castInputs.get(1)) {
                merge(output, outer, inner);
            }
        }
    }
    
    public abstract void merge(KeyedOutputStream output, Record<K, V>... inputs)
            throws IOException;
}
