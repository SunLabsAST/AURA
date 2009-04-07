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
import java.io.PrintWriter;
import java.io.Writer;

/**
 */
public class SplitKeyedOutputStream<K, V> implements KeyedOutputStream<K, V> {
    PrintWriter output;
    String separator = "\t";

    public SplitKeyedOutputStream(Writer output) {
        if(output instanceof PrintWriter) {
            this.output = (PrintWriter)output;
        } else {
            this.output = new PrintWriter(output);
        }
    }
    
    public void write(Record<K, V> record) throws IOException {
        write(record.getKey(), record.getValue());
    }
    
    public void write(K key, V value) throws IOException {
        output.print(key);
        output.print(separator);
        output.print(value);
        output.println();
    }
    
    public void close() throws IOException {
        output.close();
    }
}
