/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
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
