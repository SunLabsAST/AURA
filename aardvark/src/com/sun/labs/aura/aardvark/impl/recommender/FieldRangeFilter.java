/*
 * Project Aura,
 * 
 * Copyright (c) 2008,  Sun Microsystems Inc
 * See license.txt for licensing info.
 */
package com.sun.labs.aura.aardvark.impl.recommender;

import com.sun.labs.minion.ResultAccessor;
import java.io.Serializable;

/**
 *
 * @author plamere
 */
public class FieldRangeFilter extends ResultsFilterAdapter implements Serializable {

    private String fieldName;
    private double minValue;
    private double maxValue;

    public FieldRangeFilter(String fieldName, double minValue, double maxValue) {
        this.fieldName = fieldName;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    @Override
    protected boolean lowLevelFilter(ResultAccessor ra) {
        Object oVal = ra.getSingleFieldValue(fieldName);
        if (oVal != null) {
            System.out.println("oval class " + oVal.getClass().getSimpleName());
            if (oVal instanceof Number) {
                Number nVal = (Number) oVal;
                double val = nVal.doubleValue();
                System.out.printf("%f %f %f\n", val, minValue, maxValue);
                return val >= minValue && val < maxValue;
            } else {
                System.out.printf("false %s\n", oVal.toString());
                return false;
            }
        }
        return false;
    }
}
