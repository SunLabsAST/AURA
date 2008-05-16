package com.sun.labs.aura.util;

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
