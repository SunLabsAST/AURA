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

package com.sun.labs.aura.recommender;

import com.sun.labs.minion.ResultAccessor;
import com.sun.labs.minion.ResultsFilter;
import java.io.Serializable;

/**
 *
 */
public class LengthFilter implements ResultsFilter, Serializable {

    private String field;
    
    private int len;
    
    private int nt;
    
    private int np;
    
    public LengthFilter(String field, int len) {
        this.field = field;
        this.len = len;
    }
    
    public boolean filter(ResultAccessor ra) {
        nt++;
        String v = (String) ra.getSingleFieldValue(field);
        
        boolean ret = v != null && v.length() >= len;
        if(ret) {
            np++;
        }
        return ret;
    }

    public int getTested() {
        return nt;
    }

    public int getPassed() {
        return np;
    }
}
