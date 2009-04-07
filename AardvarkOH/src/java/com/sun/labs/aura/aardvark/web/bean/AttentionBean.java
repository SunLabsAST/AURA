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
        if (realName != null && realName.length() > 0) {
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
