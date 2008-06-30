package com.sun.labs.aura.datastore.impl.store;

import com.sun.labs.minion.ResultsFilter;
import com.sun.labs.minion.WeightedField;
import java.io.Serializable;

/**
 * A configuration that will control how a find similar runs.
 */
public class FindSimilarConfig implements Serializable {
    
    private String field;
    
    private WeightedField[] fields;
    
    private int n;
    
    private ResultsFilter filter;
    
    /**
     * Generate a default configuration.
     */
    public FindSimilarConfig() {
        n = 10;
    }
    
    public FindSimilarConfig(int n) {
        this.n = n;
    }
    
    public FindSimilarConfig(int n, ResultsFilter filter) {
        this.n = n;
        this.filter = filter;
    }
    
    public FindSimilarConfig(String field) {
        this.field = field;
        n = 10;
    }
    
    public FindSimilarConfig(String field, int n) {
        this.field = field;
        this.n = n;
    }
    
    public FindSimilarConfig(String field, int n, ResultsFilter filter) {
        this.field = field;
        this.n = n;
        this.filter = filter;
    }

    public FindSimilarConfig(WeightedField[] fields, int n, ResultsFilter filter) {
        this.fields = fields;
        this.n = n;
        this.filter = filter;
    }
    
    public String getField() {
        return field;
    }
    
    public WeightedField[] getFields() {
        return fields;
    }
    
    public int getN() {
        return n;
    }
    
    public ResultsFilter getFilter() {
        return filter;
    }
    
    
}
