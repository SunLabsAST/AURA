package com.sun.labs.aura.datastore.impl.store;

import com.sun.labs.minion.ResultsFilter;
import com.sun.labs.minion.WeightedField;
import java.io.Serializable;

/**
 * A configuration that will control how a find similar runs.
 */
public class SimilarityConfig implements Serializable {
    
    private String field;
    
    private WeightedField[] fields;
    
    private int n = 10;
    
    private ResultsFilter filter;
    
    /**
     * The percentage of terms that should be used when doing find similars using
     * this config.
     */
    private double skimPercent = 0.25;
    
    /**
     * The percentage of clusters that need to report results before the result
     * set will be used.  Defaults to 0.75.
     */
    private double reportPercent = 0.75;
    
    /**
     * The timeout (in ms) after which we will stop waiting for more results.
     * Defaults to zero, which means that no timeout will be used.
     */
    private long timeout;
    
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
        this.fields = fields;
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
        this.fields = fields;
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
