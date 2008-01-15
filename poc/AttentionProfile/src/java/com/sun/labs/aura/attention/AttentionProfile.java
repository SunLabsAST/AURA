/*
 *  Copyright 2007 Sun Microsystems, Inc. 
 *  All Rights Reserved. Use is subject to license terms.
 * 
 *  See the file "license.terms" for information on usage and
 *  redistribution of this file, and for a DISCLAIMER OF ALL
 *  WARRANTIES. 
 */
package com.sun.labs.aura.attention;

import com.sun.labs.search.music.minidatabase.MusicDatabase;
import com.sun.labs.search.music.web.apml.APML;
import com.sun.labs.search.music.web.apml.DeliciousProfileRetriever;
import com.sun.labs.search.music.web.apml.MiniDBLastFMProfileRetriever;
import com.sun.labs.search.music.web.apml.PandoraProfileRetriever;
import com.sun.labs.search.music.web.apml.APMLRetriever;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author plamere
 */
public class AttentionProfile extends HttpServlet {

    private int pagesServed;
    private int errors;
    //private LastFMProfileRetriever lcr;
    private MiniDBLastFMProfileRetriever lcr;
    private DeliciousProfileRetriever dcr;
    private PandoraProfileRetriever pcr;

    private Set<String> requests = new HashSet<String>();
    private long sumTime = 0L;
    private long minTime = Long.MAX_VALUE;
    private long maxTime = 0;
    private String VERSION = "TasteBroker Version 0.7";
    private Date startupTime = new Date();
    private MusicDatabase mdb;

    @Override
    public void init(ServletConfig sc) throws ServletException {
        try {
            String path = sc.getServletContext().getInitParameter("dbPath");
            mdb = new MusicDatabase(path);

            lcr = new MiniDBLastFMProfileRetriever(mdb);
            //lcr = new LastFMProfileRetriever();
            dcr = new DeliciousProfileRetriever();
            pcr = new PandoraProfileRetriever(lcr);
        } catch (IOException ex) {
            throw new ServletException("Can't initialize, " + ex.getMessage(), ex);
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String[] restfulParams = request.getPathInfo().split("/");

        // System.out.println("[" + request.getPathInfo() + "]");

        if (restfulParams.length == 2 && restfulParams[1].equals("stats")) {
            showStats(request, response);
            return;
        }

        if (restfulParams.length != 3) {
            response.setStatus(response.SC_BAD_REQUEST);
            errors++;
            return;
        }

        requests.add(request.getPathInfo());

        String op = restfulParams[1];
        String name = restfulParams[2];

        if (op.equals("music") | op.equals("last.fm")) {
            processAPMLRequest(lcr, name, response);
        } else if (op.equals("web")) {
            processAPMLRequest(dcr, name, response);
        } else if (op.equals("pandora")) {
            processAPMLRequest(pcr, name, response);
        } else {
            response.setStatus(response.SC_BAD_REQUEST);
            errors++;
        }
    }

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processAPMLRequest(APMLRetriever cr, String name, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {

            long startTime = System.currentTimeMillis();
            APML apml = null;
            
            apml = cr.getAPMLForUser(name);
            long deltaTime = System.currentTimeMillis() - startTime;
            sumTime += deltaTime;
            if (deltaTime < minTime) {
                minTime = deltaTime;
            }

            if (deltaTime > maxTime) {
                maxTime = deltaTime;
            }

            out.println(apml.toString());
            pagesServed++;
        } catch (IOException ex) {
            errors++;
            response.setStatus(response.SC_NOT_FOUND);
            return;
        } finally {
            out.close();
        }
    }

    protected void showStats(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<head>");
        out.println("<title> TasteBroker Statistics</title>");
        out.println("<link rel=\"stylesheet\" href=\"../styles.css\">");
        out.println("</head>");
        out.println("<body>");

        out.println("<h1> TasteBroker Statistics</h1>");
        out.println("<table>");
        out.printf("<tr><th>%s<td>%s\n", "Version", VERSION);
        out.printf("<tr><th>%s<td>%s\n", "Started on", startupTime.toString());
        out.printf("<tr><th>%s<td>%d\n", "Requests", pagesServed);
        out.printf("<tr><th>%s<td>%d\n", "Uniques", requests.size());
        out.printf("<tr><th>%s<td>%d\n", "Bad Requests", errors);
        if (pagesServed > 0) {
            out.printf("<tr><th>%s<td>%d ms\n", "Avg time", sumTime / pagesServed);
        }
        out.printf("<tr><th>%s<td>%d ms\n", "Min time", minTime);
        out.printf("<tr><th>%s<td>%d ms\n", "Max time", maxTime);
        out.println("</table>");
        out.println("<h2> Visitors </h2>");
        List<String> visitors = new ArrayList<String>(requests);
        Collections.sort(visitors);
        for (String v : visitors) {
            v = v.replaceAll("^/", "");
            out.print("<a href=\""+  v + "\">" + v + "</a> ");
        }
        out.println("</body>");
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo() {
        return "apml generator";
    }
    // </editor-fold>
}
