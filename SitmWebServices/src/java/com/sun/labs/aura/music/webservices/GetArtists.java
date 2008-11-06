/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.webservices.ItemFormatter.OutputType;
import com.sun.labs.aura.music.webservices.Util.ErrorCode;
import com.sun.labs.aura.util.AuraException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author plamere
 */
public class GetArtists extends HttpServlet {

    private final static String SERVLET_NAME = "GetArtists";
    private ParameterChecker pc;

    @Override
    public void init() throws ServletException {
        super.init();
        pc = new ParameterChecker(SERVLET_NAME, "gets the most popular artists");
        pc.addParam("max", "100", "the maximum number of results returned");
        pc.addParam("outputType", OutputType.Tiny.name(), "the type of output");
    }

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (pc.processDocumentationRequest(request, response)) {
            return;
        }

        Status status = new Status(request);
        ServletContext context = getServletContext();
        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            Util.tagOpen(out, SERVLET_NAME);
            pc.check(status, request);
            MusicDatabase mdb = DatabaseBroker.getMusicDatabase(context);
            ItemFormatterManager formatter = DatabaseBroker.getItemFormatterManager(context);

            if (mdb == null || formatter == null) {
                status.addError(ErrorCode.InternalError, "Can't connect to the music database");
                return;
            }
            int maxCount = pc.getParamAsInt(status, request, "max", 1, 10000);
            OutputType outputType = (OutputType) pc.getParamAsEnum(status, request, "outputType", OutputType.values());

            List<Artist> artists = mdb.artistGetMostPopular(maxCount);

            for (Artist artist : artists) {
                out.println(formatter.toXML(artist.getItem(), outputType, (double) mdb.artistGetNormalizedPopularity(artist)));
            }

        } catch (AuraException ex) {
            status.addError(ErrorCode.InternalError, "Problem accessing data", ex);
        } catch (ParameterException ex) {
        } finally {
            status.toXML(out);
            Util.tagClose(out, SERVLET_NAME);
            out.close();
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
    }// </editor-fold>
}
