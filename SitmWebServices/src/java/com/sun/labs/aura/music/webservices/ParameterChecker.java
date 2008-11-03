/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.music.webservices.Util.ErrorCode;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author plamere
 */
public class ParameterChecker {
    private String name;
    private String description;
    private Map<String, Parameter> allParams = new HashMap<String, Parameter>();
    private Set<String> requiredParams = new HashSet<String>();

    public ParameterChecker(String name, String description) {
        this.name = name;
        this.description = description;
        addParam("showDocumentation", "false", "shows documentation for this web service");
        addParam("debug", "false", "adds exception stack traces to the results to aid debugging");
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void addParam(String name, String description) {
        addParam(name, true, null, description);
    }

    public void addParam(String name, String defaultValue, String description) {
        addParam(name, false, defaultValue, description);
    }

    public void addParam(String name, boolean required, String defaultValue, String description) {
        Parameter p = new Parameter(name, required, defaultValue, description);
        allParams.put(name, p);
        if (required) {
            requiredParams.add(name);
        }
    }

    public String getParam(Status status, ServletRequest request, String name) throws ParameterException {
        Parameter p = allParams.get(name);
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

        if (sval == null) {
            return null;
        }

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
    
    public boolean processDocumentationRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String showDoc = request.getParameter("showDocumentation");
        if (showDoc != null && showDoc.equalsIgnoreCase("true")) {
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("<html>");
            showHead(out);
            showBody(out);
            out.println("</html>");
            return true;
        }
        return false;
    }

    private void showHead(PrintWriter out) throws IOException {
        out.println("<head>");
        out.printf("    <title>%s</title>\n", getName());
        out.println("</head>");
    }

    private void showBody(PrintWriter out) throws IOException {
        out.println("<body>");
        out.printf("    <h2>%s</h2>\n", getName());
        out.printf("    <p>%s</p>\n", getDescription());
        out.printf("    <h3>%s</h2>\n", "Parameters");
        out.printf("    <table>\n");
        out.printf("    <tr><th>Parameter<th>Required<th>Default<th>Description\n");

        List<String> keys = new ArrayList<String>(allParams.keySet());
        Collections.sort(keys);
        for (String key : keys) {
            Parameter p = allParams.get(key);
            String defaultValue = p.getDefaultValue();
            if (defaultValue == null) {
                defaultValue = "";
            }
            String required = p.isRequired() ? "<b>yes</b>" : "";
            out.printf("    <tr><td>%s<td>%s<td>%s<td>%s\n", key, required,
                    defaultValue, p.getDescription());
        }
        out.printf("    </table>\n");
        out.println("</body>");
    }
}

class Parameter {
    private String name;
    private boolean required;
    private String defaultValue;
    private String description;

    public Parameter(String name, boolean required, String defaultValue, String description) {
        this.name = name;
        this.required = required;
        this.defaultValue = defaultValue;
        this.description = description;
    }

    public boolean isRequired() {
        return required;
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
