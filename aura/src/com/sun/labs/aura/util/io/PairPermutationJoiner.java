/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
