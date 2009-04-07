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
import java.util.Arrays;
import java.util.List;

/**
 * Merges all permutations of pairs of RecordSets and outputs the result to
 * files.
 * 
 * @author Will Holcomb <william.holcomb@sun.com>
 */
public class PairPermutationJoiner {
    RecordMerger merger;
    OutFileGenerator outFiles;
    
    public PairPermutationJoiner(RecordMerger merger, OutFileGenerator outFiles) {
        this.merger = merger;
        this.outFiles = outFiles;
    }

    /**
     * Permutes all pairs of the given RecordSets including each set with itself.
     * 
     * @param recordSets
     * @throws java.io.IOException
     */
    public void permutePairs(List<RecordSet> recordSets) throws IOException {
        
        for(RecordSet outerSet : recordSets) {
            for(RecordSet innerSet : recordSets) {
                KeyedOutputStream output = null;
                List<RecordSet> sets = Arrays.asList(outerSet, innerSet);
                try {
                    output = new SplitKeyedOutputStream(outFiles.getOutput(sets));
                    merger.merge(sets, output);
                } finally {
                    if(output != null) {
                        output.close();
                    }
                }
            }
        }
    }
}
