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

package com.sun.labs.aura.datastore;

import com.sun.labs.minion.ResultsFilter;
import com.sun.labs.minion.WeightedField;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A configuration that will control how a find similar runs.
 */
public class SimilarityConfig implements Serializable {
    
    private String field;
    
    private WeightedField[] fields;
    
    private int n = 10;
    
    private ResultsFilter filter;
    
    private Set<String> exclude;
    private Set<String> include;
    
    /**
     * The percentage of terms that should be used when doing find similars using
     * this config.
     */
    private double skimPercent = 1;
    
    /**
     * The percentage of clusters that need to report results before the result
     * set will be used.  Defaults to 0.75.
     */
    private double reportPercent = 1;
    
    /**
     * The timeout (in ms) after which we will stop waiting for more results.
     * Defaults to zero, which means that no timeout will be used.
     */
    private long timeout;

    /**
     * Whether we're happy to get partial results.  Defaults to false.
     */
    private boolean allowPartialResults;
    
    /**
     * Generate a default configuration.
     */
    public SimilarityConfig() {
    }
    
    public SimilarityConfig(int n) {
        this.n = n;
    }
    
    public SimilarityConfig(int n, ResultsFilter filter) {
        this.n = n;
        this.filter = filter;
    }
    
    public SimilarityConfig(String field) {
        this.field = field;
        n = 10;
    }
    
    public SimilarityConfig(WeightedField[] fields) {
        if (fields != null) {
            this.fields = Arrays.copyOf(fields, fields.length);
        }
        n = 10;
    }
    
    public SimilarityConfig(String field, int n) {
        this.field = field;
        this.n = n;
    }
    
    public SimilarityConfig(String field, int n, ResultsFilter filter) {
        this.field = field;
        this.n = n;
        this.filter = filter;
    }

    public SimilarityConfig(WeightedField[] fields, int n, ResultsFilter filter) {
        if (fields != null) {
            this.fields = Arrays.copyOf(fields, fields.length);
        }
        this.n = n;
        this.filter = filter;
    }
    
    public void setSkimPercent(double skimPercent) {
        this.skimPercent = skimPercent;
    }
    
    public double getSkimPercent() {
        return skimPercent;
    }

    public double getReportPercent() {
        return reportPercent;
    }

    public void setReportPercent(double reportPercent) {
        this.reportPercent = reportPercent;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void setAllowPartialResults(boolean allowPartialResults) {
        this.allowPartialResults = allowPartialResults;
    }

    public boolean getAllowPartialResults() {
        return allowPartialResults;
    }
    
    public String getField() {
        return field;
    }
    
    public WeightedField[] getFields() {
        if (fields != null) {
            return Arrays.copyOf(fields, fields.length);
        } else {
            return null;
        }
    }
    
    public int getN() {
        return n;
    }
    
    public ResultsFilter getFilter() {
        return filter;
    }

    public void setExclude(Set<String> exclude) {
        this.exclude = exclude;
    }
    
    public Set<String> getExclude() {
        return exclude;
    }

    public void setInclude(Set<String> include) {
        this.include = include;
    }

    public Set<String> getInclude() {
        return include;
    }
    
    public Set<String> getFieldNames() {
        
        if(field == null && fields == null) {
            return null;
        } else if(fields == null) {
            return Collections.singleton(field);
        } else {
            Set<String> ret = new HashSet<String>();
            for(WeightedField wf : fields) {
                ret.add(wf.getFieldName());
            }
            return ret;
        }
    }
    
}
