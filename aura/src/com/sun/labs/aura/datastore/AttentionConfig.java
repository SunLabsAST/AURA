
package com.sun.labs.aura.datastore;

import java.io.Serializable;

/**
 * Describes the constraints placed on Attention during querying.  This class
 * is provided as a parameter to the various methods that retrieve Attention
 * objects from the DataStore.  Any value that is set in this class is
 * considered to be a required parameter for looking up the attention.
 */
public class AttentionConfig implements Serializable {
    protected String sourceKey = null;
    protected String targetKey = null;
    protected Attention.Type type = null;
    protected String stringVal = null;
    protected Long numberVal = null;

    public AttentionConfig() {
        
    }

    /**
     * Gets the constraint on the source key
     * 
     * @return the required source key
     */
    public String getSourceKey() {
        return sourceKey;
    }

    /**
     * Sets the constraint on the source key
     * 
     * @param sourceKey the source key to require
     */
    public void setSourceKey(String sourceKey) {
        if (sourceKey != null && !sourceKey.isEmpty()) {
            this.sourceKey = sourceKey;
        }
    }

    /**
     * Gets the constraint on the target key
     * 
     * @return the required target key
     */
    public String getTargetKey() {
        return targetKey;
    }

    /**
     * Sets the constraint on the target key
     * 
     * @param targetKey the target key to require
     */
    public void setTargetKey(String targetKey) {
        if (targetKey != null && !targetKey.isEmpty()) {
            this.targetKey = targetKey;
        }
    }

    /**
     * Gets the constraint on the attention type
     * 
     * @return the required type of attention
     */
    public Attention.Type getType() {
        return type;
    }

    /**
     * Sets the constraint on the attention type
     * 
     * @param type the type of attention to require
     */
    public void setType(Attention.Type type) {
        this.type = type;
    }

    /**
     * Gets the constraint on the attention's string value
     * 
     * @return the required string value
     */
    public String getStringVal() {
        return stringVal;
    }

    /**
     * Sets the contraint on the attention's string value
     * 
     * @param stringVal the string value to require
     */
    public void setStringVal(String stringVal) {
        if (stringVal != null && !stringVal.isEmpty()) {
            this.stringVal = stringVal;
        }
    }

    /**
     * Gets the constraint on the attention's numeric value
     * 
     * @return the required numeric value
     */
    public Long getNumberVal() {
        return numberVal;
    }

    /**
     * Sets the constraint on the attention's numeric value
     * 
     * @param numberVal the numeric value to require
     */
    public void setNumberVal(Long numberVal) {
        this.numberVal = numberVal;
    }
}
