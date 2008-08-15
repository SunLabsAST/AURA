/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.util.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Will Holcomb <william.holcomb@sun.com>
 */
public class SplitKeyedInputStream<K, V> implements KeyedInputStream {
    RandomAccessFile input;
    String splitString;
    int keyIndex;
    
    public SplitKeyedInputStream(RandomAccessFile input, String splitString) {
        this(input, splitString, 0);
    }
    
    
    public SplitKeyedInputStream(RandomAccessFile input, String splitString, int keyIndex) {
        this.splitString = splitString;
        this.keyIndex = keyIndex;
        // I can find no file-backed reader that supports the reset() method
        /*
        if(input instanceof BufferedReader) {
            this.input = (BufferedReader)input;
        } else {
            this.input = new BufferedReader(input);
        }
         */
        this.input = input;
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

    public boolean isSorted() {
        return false;
    }
    
    public void reset() throws IOException {
        input.seek(0);
    }
    
    public static void main(String... args) throws FileNotFoundException, IOException {
        RandomAccessFile reader = new RandomAccessFile(args[0], "r");
        String splitString = "\t";
        if(args.length > 1) {
            splitString = args[1];
        }
        SplitKeyedInputStream source = new SplitKeyedInputStream(reader, splitString, 1);

        Record<String, List<String>> record;
        while((record = source.read()) != null) {
            System.out.println("Record: " + record.getKey());
        }
    }
}
