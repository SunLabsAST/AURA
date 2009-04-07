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
