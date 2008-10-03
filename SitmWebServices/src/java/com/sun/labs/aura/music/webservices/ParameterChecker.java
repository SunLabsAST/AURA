/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.music.webservices.Util.ErrorCode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletRequest;

/**
 *
 * @author plamere
 */
public class ParameterChecker {

    private Map<String, Parameter> allParams = new HashMap<String, Parameter>();
    private Set<String> requiredParams = new HashSet<String>();

    public void addParam(String name, String description) {
        addParam(name, true, null, description);
    }

    public void addParam(String name, String defaultValue, String description) {
        addParam(name, false, defaultValue, description);
    }

    public void addParam(String name, boolean required, String defaultValue, String description) {
        Parameter p = new Parameter(name, defaultValue, description);
        allParams.put(name.toLowerCase(), p);
        if (required) {
            requiredParams.add(name.toLowerCase());
        }
    }

    public String getParam(Status status, ServletRequest request, String name) throws ParameterException {
        Parameter p = allParams.get(name.toLowerCase());
        if (p == null) {
            status.addError(ErrorCode.InternalError, "No parameter configuration for requested param " + name);
            throw new ParameterException();
        } else {
            String value = request.getParameter(name);
            if (value == null) {
                value = p.getDefaultValue();
            }
            return value;
        }
    }

    public int getParamAsInt(Status status, ServletRequest request, String name) throws ParameterException {
        String sval = getParam(status, request, name);
        try {
            int val = Integer.parseInt(sval);
            return val;
        } catch (NumberFormatException ex) {
            status.addError(ErrorCode.BadArgument, "bad number format for " + name);
            throw new ParameterException();
        }
    }

    public int getParamAsInt(Status status, ServletRequest request, String name, int min, int max) throws ParameterException {
        int val = getParamAsInt(status, request, name);
        if (val < min || val > max) {
            status.addError(ErrorCode.BadArgument, name + " out of range: " + min + " to " + max);
            throw new ParameterException();
        }
        return val;
    }

    public Enum getParamAsEnum(Status status, ServletRequest request, String name, Enum[] vals) throws ParameterException {
        String sval = getParam(status, request, name);
        for (Enum v : vals) {
            if (v.name().equalsIgnoreCase(sval)) {
                return v;
            }
        }
        StringBuilder sb = new StringBuilder("bad value for " + name + ". Should be one of: ");
        for (Enum v : vals) {
            sb.append(v.name());
            sb.append(" ");
        }
        status.addError(ErrorCode.BadArgument, sb.toString());
        throw new ParameterException();
    }

    public void check(Status status, ServletRequest request) throws ParameterException {
        Set<String> curSet = new HashSet<String>();

        Set keys = request.getParameterMap().keySet();

        // check to make sure that only expected params are present

        for (Object okey : keys) {
            String key = (String) okey;
            key = key.toLowerCase();
            curSet.add(key);
            if (allParams.get(key) == null) {
                status.addError(ErrorCode.BadArgument, "Unknown parameter " + key);
            }
        }

        // check to make sure that all required params are present.

        for (String rparam : requiredParams) {
            if (!curSet.contains(rparam)) {
                status.addError(ErrorCode.MissingArgument, "missing " + rparam);
            }
        }

        if (!status.isOK()) {
            throw new ParameterException();
        }
    }
}

class Parameter {

    String name;
    String defaultValue;
    String description;

    public Parameter(String name, String defaultValue, String description) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.description = description;
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

}
