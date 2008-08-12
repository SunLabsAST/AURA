/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.util.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Will Holcomb <william.holcomb@sun.com>
 */
public class SplitKeyRecordSource<K, V> implements RecordInputStream {
    BufferedReader input;
    String splitString;
    int keyIndex;
    
    public SplitKeyRecordSource(Reader input, String splitString) {
        this(input, splitString, 0);
    }
    
    public SplitKeyRecordSource(Reader input, String splitString, int keyIndex) {
        this.splitString = splitString;
        this.keyIndex = keyIndex;
        if(input instanceof BufferedReader) {
            this.input = (BufferedReader)input;
        } else {
            this.input = new BufferedReader(input);
        }
    }
    
    public Record<String, List<String>> read() throws IOException {
        Record<String, List<String>> record = null;
        String nextline = null;
        try {
            do {
                nextline = input.readLine();
            } while(nextline.equals(""));
            String[] values = nextline.split(splitString);
            record = new Record(values[keyIndex], Arrays.asList(values));
        } catch(NullPointerException npe) {
            // reached eof, do nothing and return null
        }
        return record;
    }
}
