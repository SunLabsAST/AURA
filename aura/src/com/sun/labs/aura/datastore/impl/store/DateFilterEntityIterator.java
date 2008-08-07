/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.datastore.impl.store;

import com.sleepycat.je.Transaction;
import com.sleepycat.persist.ForwardCursor;
import com.sun.labs.aura.datastore.Attention;
import java.util.Date;

/**
 * An entity iterator that filters returned Attentions based on date.  Only
 * Attentions that occurred on or after the specified date are returned.
 * 
 */
public class DateFilterEntityIterator extends EntityIterator {
    private Date afterDate;
    private Attention curr;
    
    public DateFilterEntityIterator(ForwardCursor<Attention> cursor, Date timeStamp) {
        super(cursor);
        afterDate = (Date)timeStamp.clone();
    }
    
    public DateFilterEntityIterator(ForwardCursor<Attention> cursor,
                                    Transaction txn,
                                    Date timeStamp) {
        super(cursor, txn);
        afterDate = (Date)timeStamp.clone();
    }
    
    @Override
    public boolean hasNext() {
        //
        // If we have one waiting, there is a next
        if (curr != null) {
            return true;
        }
        
        //
        // Otherwise, scan through until we find one or we reach the end
        do {
            if (it.hasNext()) {
                curr = (Attention)it.next();
            } else {
                curr = null;
            }
        } while (curr != null && curr.getTimeStamp() < afterDate.getTime());
        
        //
        // If we have one, there is a next
        if (curr != null) {
            return true;
        }
        return false;
    }

    @Override
    public Attention next() {
        //
        // Call hasNext to cue up the next one and return whatever
        // we got from it (a value or null).
        hasNext();

        //
        // Return it and blank out the next
        Attention ret = curr;
        curr = null;
        return ret;
    }

}
