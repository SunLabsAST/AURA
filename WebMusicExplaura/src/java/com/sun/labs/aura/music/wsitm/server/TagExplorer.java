/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.wsitm.server;


import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.recommender.TypeFilter;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.Tag;
import java.io.*;
import java.net.*;

import java.util.List;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author plamere
 */
public class TagExplorer extends HttpServlet {

    private DataManager dm;
    private DataStore datastore;
    private Logger logger;

    @Override
    public void init(ServletConfig sc) throws ServletException {
        super.init(sc);
        dm = ServletTools.getDataManager(sc);
        datastore = dm.getDataStore();
        //@todo fix this
        //logger = dm.getLogger();
        //logger.log("_system_", "startup", "");
    }

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int count = 1000;
        String scount = request.getParameter("count");
        if (scount != null) {
            count = Integer.parseInt(scount);
        }
        
        String tagName = request.getParameter("tag");

        if (tagName == null) {
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            try {
                out.println("<html>");
                out.println("<head>");
                out.println("<title>Tag Explorer</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("<h1>Tag Explorer</h1>");
                //@todo fix this
                /*
                 List<Tag> tags = datastore.query("=5", count, new TypeFilter(Item.ItemType.));//mdb.getTags(count);

                out.println("<table>");
                for (Tag tag : tags) {
                    String ltag = tag.getName().replaceAll(" ", "+");
                    //@todo fix this
                    //out.printf("<tr> <td> <a href=\"TagExplorer?count=1000&tag=%s\">%s</a></td><td>%.4f</td></tr>\n", ltag, tag.getName(), tag.getPopularity());
                }
                 * */
                out.println("</table>");
                out.println("</body>");
                out.println("</html>");
            } finally {
                out.close();
            }
        } else {
            response.setContentType("text/plain;charset=UTF-8");
            PrintWriter out = response.getWriter();
            try {
                String tname = tagName.replaceAll("\\+", " ");
                //@todo fix this
                //Tag tag = mdb.getTagByName(tname);
                //List<Scored<Artist>> mostRepresentativeArtist = tag.getMostRepresentativeArtists(count);
                //dumpArtist(out, tname, "weight", mostRepresentativeArtist);
                //List<Scored<Artist>> mostTaggedArtist = tag.getMostTaggedArtists(count);

                //dumpArtist(out, tname, "count", mostTaggedArtist);
            } finally {
                out.close();
            }
        }
    }
    
    private void dumpArtist(PrintWriter out, String tagName, String order, List<Scored<Artist>> artists) {
        int index = 1;

        out.println();
        out.println("# Artists tagged with '" +tagName + "' ordered by " + order);
        out.println();
        for (Scored<Artist> scored : artists) {
            out.printf("%6d %s  %s  %f %s\n", index++, order, scored.getItem().getKey(), scored.getScore(), scored.getItem().getName());
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Short description";
    }
    // </editor-fold>
}
