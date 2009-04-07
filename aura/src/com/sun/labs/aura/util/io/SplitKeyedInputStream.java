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
