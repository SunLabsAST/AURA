/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.util.AuraException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author plamere
 */
public class Dashboard extends StandardService {

    @Override
    public void initParams() {
        setDocType(DocType.HTML);
    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void go(HttpServletRequest request, PrintWriter out, MusicDatabase mdb)
            throws AuraException, ParameterException, RemoteException {
        StatsManager sm = DatabaseBroker.getStatsManager(getServletContext());
        List<String> servletNames = sm.getNames();

        long totalCalls = 0;
        long totalErrors = 0;
        header(out);

        
        out.println("<h1> WebServices Dashboard for " + request.getServerName() +"</h1>");
        out.println("<h2> Servlet statistics</h2>");
        out.println("<table>");
        out.printf("<tr><th>Servlet<th>Calls<th>Errs<th>AvgTime<th>LastTime<th>MinTime<th>MaxTime</tr>");
        for (String servlet : servletNames) {
            Stats stats = sm.getStats(servlet);
            long avg = 0;
            if (stats.getGoodCount() > 0) {
                avg = stats.getGoodTime() / stats.getGoodCount();
            }
            out.printf("<tr><td>%s<td>%d<td>%d<td>%d<td>%d<td>%d<td>%d</tr>",
                    servlet, stats.getGoodCount(), stats.getBadCount(),
                    avg, stats.getLastTime(), stats.getMinTime(), stats.getMaxTime());

            totalCalls += stats.getGoodCount();
            totalErrors += stats.getBadCount();
        }
        out.println("</table>");
        out.println("<p>");
        out.println("<h2> Summary statistics</h2>");
        out.println("<table>");
        out.println("<tr><th>Total Calls<td>" + totalCalls);
        out.println("<tr><th>Total Errors<td>" + totalErrors);
        out.println("<tr><th>Running since<td>" + new Date(sm.getStartTime()));
        out.println("</table>");

        out.println("<p>");
        out.println("<h2> Requestor statistics</h2>");
        out.println("<table>");
        out.printf("<tr><th>Requestor IP<th>Host<th>Count</tr>");
        Map<String, Integer> requestors = sm.getRequestors();
        List<String> addrs = new ArrayList<String>(requestors.keySet());
        Collections.sort(addrs);

        for (String addr : addrs) {
            out.printf("<tr><td>%s<td>%s<td>%d</tr>\n",
                    addr, getHostName(addr), requestors.get(addr).intValue());
        }
        out.println("</table>");

        footer(out);
    }


    private String getHostName(String addr) {
        try {
            InetAddress name = InetAddress.getByName(addr);
            return name.getHostName();
        } catch (UnknownHostException ex) {
            return "";
        }
    }

    private void header(PrintWriter out) {
        out.println("<html>");
        out.println("   <head>");
        out.println("       <title>SITM Web Services Stats</title>");
        out.println("       <link rel=\"stylesheet\" type=\"text/css\" href=\"styles/dashboard.css\">");
        out.println("   </head>");
        out.println("   <body>");
    }

    private void footer(PrintWriter out) {
        out.println("   </body>");
        out.println("</html>");
    }

    /**
     * Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo() {
        return "Shows information about servlets";
    }
}