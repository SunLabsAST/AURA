package com.sun.labs.aura.util.io;

import com.sun.labs.util.SimpleLabsLogFormatter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Logger;
import ngnova.util.ChannelUtil;

/**
 * Sorts a keyed input stream.
 */
public class Sorter<K, V> {

    private File in;

    private File bf;

    private File sf;

    private int buffSize;
    
    /**
     * The default buffer size for the blocks to sort.
     */
    public static final int DEFAULT_BUFFER_SIZE = 8 * 1024;

    /**
     * Creates a sorter that will sort records from the input into the sorted
     * file.  We use an intermediate file to hold sorted blocks from the 
     * input before the merge.
     * @param in the input file
     * @param bf a temporary output file for blocks.   This file will be overwritten
     * if it exists and deleted at the end of sorting
     * @param sf the sorted output file.  This file will be overwritten.
     */
    public Sorter(File in, File bf, File sf) throws FileNotFoundException, IOException {
        this(in, bf, sf, DEFAULT_BUFFER_SIZE);
    }
    
    /**
     * Creates a sorter that will sort records from the input into the sorted
     * file.  We use an intermediate file to hold sorted blocks from the 
     * input before the merge.
     * @param in the input file
     * @param bf a temporary output file for blocks.   This file will be overwritten
     * if it exists and deleted at the end of sorting
     * @param sf the sorted output file.  This file will be overwritten.
     * @param buffSize the amount of data from the file to buffer in memory.
     */
    public Sorter(File in, File bf, File sf, int buffSize) throws FileNotFoundException, IOException {
        this.in = in;
        this.bf = bf;
        this.sf = sf;
        this.buffSize = buffSize;
    }

    public void sort() throws IOException {

        KeyedInputStream<K, V> input = new KeyedInputStream<K, V>(in);
        
        //
        // If it's already sorted, we're done!
        if(input.getSorted()) {
            KeyedOutputStream<K,V> output = new KeyedOutputStream<K, V>(sf, true);
            ChannelUtil.transferFully(input.getChannel(), output.getChannel());
            input.close();
            output.close();
            return;
        }
        
        KeyedOutputStream<K, V> output = new KeyedOutputStream<K, V>(bf, false);

        Record<K, V> rec;

        List<Record<K, V>> recs = new ArrayList<Record<K, V>>();

        List<SortedRegion> regions = new ArrayList<SortedRegion>();

        SortedRegion cr = new SortedRegion(input.position());

        //
        // Read to the end of file, building up a list of records until we
        // pass the buffer size then sorting that region and writing it to the 
        // blocked file.
        while((rec = input.read()) != null) {
            recs.add(rec);
            long len = input.position() - cr.start;
            if(len >= buffSize) {
                cr = writeSortedRecords(input, cr, regions, recs, output);
            }
        }
        
        //
        // Deal with the last region.
        if(recs.size() > 0) {
            cr = writeSortedRecords(input, cr, regions, recs, output);
        }
        
        input.close();
        output.close();
        
        //
        // If we only had one region, then we're done.  Just rename the blocked
        // file to the sorted file.
        if(regions.size() == 0) {
           bf.renameTo(sf);
           return;
        }

        //
        // OK, now the blocked file contains a number of sorted regions.  We want
        // to merge those regions.  We'll just pretend that they're keyed input
        // files and use the merger.
        List<KeyedInputStream<K,V>> inputs = new ArrayList<KeyedInputStream<K, V>>();
        for(SortedRegion sr : regions) {
            inputs.add(new KeyedInputStream<K, V>(bf, sr));
        }
        Merger m = new Merger();
        output = new KeyedOutputStream<K, V>(sf, true);
        m.merge(inputs, output, null);
        for(KeyedInputStream<K,V> kis : inputs) {
            kis.close();
        }
        output.close();
        bf.delete();
    }
    
    private SortedRegion writeSortedRecords(KeyedInputStream<K,V> input, 
            SortedRegion cr, 
            List<SortedRegion> regions, 
            List<Record<K,V>> recs, 
            KeyedOutputStream<K,V> output) throws IOException {
        //
        // We've exceeded the buffer size, so sort this block and write
        // it to the output file.
        long obp = output.position();
        long pos = input.position();
        cr.setLen(pos - cr.start);
        cr.setSize(recs.size());
        Collections.sort(recs);
        for(Record<K, V> r : recs) {
            output.write(r);
        }
        recs.clear();

        //
        // Save this region.
        regions.add(cr);
        cr = new SortedRegion(pos);
        return cr;
    }

    public class SortedRegion {

        protected int size;

        protected long start;

        protected long len;

        public SortedRegion(long start) {
            this.start = start;
        }

        public void setLen(long len) {
            this.len = len;
        }

        public void setSize(int size) {
            this.size = size;
        }
        
        public String toString() {
            return String.format("size: %d start: %d len: %d", size, start, len);
        }
    }
    
    public static void main(String[] args) throws Exception {
        //
        // Use the labs format logging.
        Logger rl = Logger.getLogger("");
        for(Handler h : rl.getHandlers()) {
            h.setFormatter(new SimpleLabsLogFormatter());
        }

        Sorter s = new Sorter(new File(args[0]), 
                new File(args[1]), new File(args[2]));
        s.sort();
    }
}
