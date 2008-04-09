
package com.sun.labs.aura.aardvark.web.bean;

/**
 * Bean for representing an attention
 */
public class AttentionBean {
    protected String srcKey;
    protected String targetKey;
    protected String type;
    protected String time;
    protected String realName;
    
    public AttentionBean() {
        
    }
    
    public AttentionBean(String src, String target, String type, String time) {
        this.srcKey = src;
        this.targetKey = target;
        this.type = type;
        this.time = time;
    }
    
    public void setRealName(String realName) {
        this.realName = realName;
    }
    
    public String getType() {
        return type;
    }

    public String getSrcKey() {
        return srcKey;
    }

    public String getTargetKey() {
        return targetKey;
    }
    
    public String getTargetKeyName() {
        if (realName != null) {
            if (realName.length() > 73) {
                return realName.substring(0, 70) + "...";
            }
            return realName;
        }
        if (targetKey.length() > 73) {
            return targetKey.substring(0, 70) + "...";
        }
        return targetKey;
    }

    public String getTime() {
        return time;
    }
}
