/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.admin.server;

import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.admin.client.WorkbenchDescriptor;
import com.sun.labs.aura.music.admin.client.WorkbenchResult;
import com.sun.labs.aura.util.AuraException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author plamere
 */
public abstract class Worker extends MDBHelper {

    private WorkbenchDescriptor descriptor;
    private Map<String, Enum[]> enumMap = new HashMap<String, Enum[]>();

    public Worker(String name, String description) {
        descriptor = new WorkbenchDescriptor(name, description);
    }

    public WorkbenchDescriptor getDescriptor() {
        return descriptor;
    }

    protected void param(String name, String description, String defaultValue) {
        descriptor.addParam(name, description, defaultValue);
    }

    protected void param(String name, String description, int defaultValue) {
        descriptor.addParam(name, description, defaultValue);
    }

    protected void param(String name, String description, float defaultValue) {
        descriptor.addParam(name, description, defaultValue);
    }

    protected void param(String name, String description, Enum[] values, Enum defaultValue) {
        descriptor.addParam(name, description, getNames(values), defaultValue.name());
        enumMap.put(name, values);
    }

    protected void param(String name, String description, String[] values, String defaultValue) {
        descriptor.addParam(name, description, values, defaultValue);
    }

    private String[] getNames(Enum[] values) {
        String[] names = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            names[i] = values[i].name();
        }
        return names;
    }

    public WorkbenchResult runWorker(MusicDatabase mdb, Map<String, String> params) {
        WorkbenchResult result = new WorkbenchResult();
        long start = System.currentTimeMillis();
        try {
            go(mdb, params, result);
        } catch (Throwable t) {
            result.fail(getExceptionExplanation(t));
            captureStackTrace(result, t);
        } 
        long delta = System.currentTimeMillis() - start;
        result.setTime(delta);
        return result;
    }

    abstract void go(MusicDatabase mdb, Map<String, String> params, WorkbenchResult result) throws AuraException, RemoteException;

    protected String getParam(Map<String, String> p, String name) throws AuraException {
        String v = p.get(name);
        if (v == null) {
            throw new AuraException("Missing parameter " + name);
        } else {
            return v;
        }
    }

    protected int getParamAsInt(Map<String, String> p, String name) throws AuraException {
        String v = getParam(p, name);
        int val = 0;
        try {
            val = Integer.parseInt(v);
        } catch (NumberFormatException e) {
            throw new AuraException("Bad numeric format for " + name);
        }
        return val;
    }

    protected boolean getParamAsBoolean(Map<String, String> p, String name) throws AuraException {
        String v = getParam(p, name);

        if (v.equalsIgnoreCase("yes")) {
            return true;
        } else if (v.equalsIgnoreCase("true")) {
            return true;
        } else if (v.equalsIgnoreCase("no")) {
            return false;
        } else if (v.equalsIgnoreCase("false")) {
            return false;
        }

        throw new AuraException("bad boolean value for " + name);
    }

    protected float getParamAsFloat(Map<String, String> p, String name) throws AuraException {
        String v = getParam(p, name);
        float val = 0;
        try {
            val = Float.parseFloat(v);
        } catch (NumberFormatException e) {
            throw new AuraException("Bad numeric format for " + name);
        }
        return val;
    }

    protected Enum getParamAsEnum(Map<String, String> p, String name) throws AuraException {
        String v = getParam(p, name);
        Enum[] enums = enumMap.get(name);

        if (enums == null) {
            throw new AuraException("Can't getParamAsEnum, not an enum type " + name);
        }

        for (Enum e : enums) {
            if (e.name().equalsIgnoreCase(v)) {
                return e;
            }
        }

        throw new AuraException("Can't find enum for value " + v);
    }


    private String getExceptionExplanation(Throwable t) {
        String exceptionName = t.getClass().getSimpleName();
        String msg = t.getMessage();
        if (t.getCause() != null) {
            msg = msg + " cause: " + getExceptionExplanation(t.getCause());
        }
        return exceptionName + ":" + msg;
    }

    private void captureStackTrace(WorkbenchResult result, Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.close();
        String trace = sw.toString();
        if (trace.length() > 0) {
            result.output(trace);
        }
    }
}
