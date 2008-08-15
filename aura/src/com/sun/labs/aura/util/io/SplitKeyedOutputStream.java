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
