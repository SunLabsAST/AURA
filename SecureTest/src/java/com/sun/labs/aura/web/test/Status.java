/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.web.test;

import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.AttentionConfig;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.util.AuraException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author stgreen
 */
public class Status extends HttpServlet {

    protected static DataStore ds;

    protected static Logger logger = Logger.getLogger("");

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ServletContext context = getServletContext();
        ds = (DataStore) context.getAttribute("dataStore");
    }

    void tagOpen(PrintWriter out, String tag) {
        out.println("<" + tag + ">");
    }

    void tagClose(PrintWriter out, String tag) {
        out.println("</" + tag + ">");
    }

    void tag(PrintWriter out, String tag, String val) {
        tagOpen(out, tag);
        out.println(val);
        tagClose(out, tag);
    }

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            tagOpen(out, "status");
            // show the number of items of each type
            if(ds != null) {

                tag(out, "ready", "" + ds.ready());
                tag(out, "replicants",
                        Integer.toString(ds.getPrefixes().size()));
                for(ItemType t : ItemType.values()) {
                    long count = ds.getItemCount(t);
                    if(count > 0L) {
                        tag(out, t.toString(), Long.toString(count));
                    }
                }
                for(Attention.Type t : Attention.Type.values()) {
                    AttentionConfig ac = new AttentionConfig();
                    ac.setType(t);
                    long count = ds.getAttentionCount(ac);
                    if(count > 0L) {
                        tag(out, t.toString(), Long.toString(count));
                    }
                }
            } else {
                tag(out, "ready", "DataStore not found!");
            }
            tagClose(out, "status");
        } catch (AuraException ex) {
            logger.severe("Error accessing datastore: " + ex);
        } finally {
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
