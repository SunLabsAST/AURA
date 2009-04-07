/*
 * Copyright 2008-2009 Sun Microsystems, Inc. All Rights Reserved.
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
