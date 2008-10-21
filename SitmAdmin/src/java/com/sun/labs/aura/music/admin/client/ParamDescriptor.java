/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */

package com.sun.labs.aura.music.admin.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 *
 * @author plamere
 */
public class ParamDescriptor implements IsSerializable {
    public enum Type { TypeString, TypeNumeric, TypeEnum};

    private String name;
    private String description;
    private String defaultValue;
    private Type type;
    private String[] enumValues;

    public ParamDescriptor() {
    }

    public ParamDescriptor(String name, String description, String defaultValue, Type type, String[] enumValues) {
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;
        this.type = type;
        this.enumValues = enumValues;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public String[] getEnumValues() {
        return enumValues;
    }
}
