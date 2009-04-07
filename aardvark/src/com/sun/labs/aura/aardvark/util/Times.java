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

package com.sun.labs.aura.aardvark.util;

import java.util.Date;

/**
 * Some times in milliseconds for doing our time based queries
 *
 */
public class Times {
    public static final long ONE_MINUTE = 1000L * 60;
    
    public static final long ONE_HOUR = 60 * ONE_MINUTE;
    
    public static final long ONE_DAY = 24 * ONE_HOUR;
    
    public static final long ONE_WEEK = 7 * ONE_DAY;
    
    public static final long ONE_MONTH = 30 * ONE_DAY;
    
    public static final long ONE_YEAR = 365 * ONE_DAY;

    /**
     * Returns a date that is the given number of days ago
     * @param days the number of days to go back in time
     * @return a date representation o
     */
    public static Date getDaysAgo(int days) {
        long deltaInMillseconds = days *  Times.ONE_DAY;
        long then  = System.currentTimeMillis() - deltaInMillseconds;
        Date thenDate = new Date(then);
        return thenDate;
    }

}
