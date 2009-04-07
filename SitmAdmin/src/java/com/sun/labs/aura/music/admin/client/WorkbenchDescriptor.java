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

package com.sun.labs.aura.music.admin.client;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author plamere
 */
public class WorkbenchDescriptor implements IsSerializable {
    private String name;
    private String description;
    private List<ParamDescriptor> parameters;

    public WorkbenchDescriptor() {
        parameters = new ArrayList<ParamDescriptor>();
    }
    
    public WorkbenchDescriptor(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }

    public void addParam(String name, String description, String defaultValue) {
        ParamDescriptor p = new ParamDescriptor(name, description, defaultValue, 
                ParamDescriptor.Type.TypeString, null);
        parameters.add(p);
    }

    public void addParam(String name, String description, int defaultValue) {
        ParamDescriptor p = new ParamDescriptor(name, description, Integer.toString(defaultValue), 
                ParamDescriptor.Type.TypeNumeric, null);
        parameters.add(p);
    }

    public void addParam(String name, String description, float defaultValue) {
        ParamDescriptor p = new ParamDescriptor(name, description, Float.toString(defaultValue), 
                ParamDescriptor.Type.TypeNumeric, null);
        parameters.add(p);
    }

    public void addParam(String name, String description, String[] values, String defaultValue) {
        ParamDescriptor p = new ParamDescriptor(name, description, defaultValue, 
                ParamDescriptor.Type.TypeEnum, values);
        parameters.add(p);
    }
    
    public ParamDescriptor getParamDescriptor(String name) {
        for (ParamDescriptor p : parameters) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;

    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ParamDescriptor> getParameters() {
        return parameters;
    }

    public void setParameters(List<ParamDescriptor> params) {
        this.parameters = params;
    }
}
