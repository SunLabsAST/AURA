/*
 *  Copyright 2007 Sun Microsystems, Inc. 
 *  All Rights Reserved. Use is subject to license terms.
 * 
 *  See the file "license.terms" for information on usage and
 *  redistribution of this file, and for a DISCLAIMER OF ALL
 *  WARRANTIES. 
 */
package com.sun.labs.aura.aardvark.server;

import com.sun.labs.aura.aardvark.Aardvark;
import com.sun.labs.aura.aardvark.util.OPMLProcessor;
import com.sun.labs.aura.aardvark.store.Attention;
import com.sun.labs.aura.aardvark.store.item.User;
import com.sun.labs.aura.aardvark.util.AuraException;
import java.io.*;
import java.net.*;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 *
 * @author plamere
 */
public class OpmlUpload extends HttpServlet {

    /** 
    * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
    * @param request servlet request
    * @param response servlet response
    */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String[] restfulParams = request.getPathInfo().split("/");

        if (restfulParams.length != 2) {
            setResponse(response, "OPML Upload Error", "Can't find the user");
        } else {
            String userID = restfulParams[1];
            if (!ServletFileUpload.isMultipartContent(request)) {
                setResponse(response, "OPML Upload Error", "No uploaded file");
            } else {
                try {
                    Aardvark aardvark = (Aardvark) getServletContext().getAttribute("aardvark");

                    if (aardvark == null) {
                        setResponse(response, "OPML Upload failed", "Can't find aardvark in servlet context ");
                    }

                    User user = aardvark.getUser(userID);
                    if (user == null) {
                        setResponse(response, "OPML Upload failed", "Can't find user " + userID);
                        return;
                    }
                    OPMLProcessor op = new OPMLProcessor();
                    DiskFileItemFactory factory = new DiskFileItemFactory();

                    // Create a new file upload handler
                    ServletFileUpload upload = new ServletFileUpload(factory);

                    // Parse the request
                    List items = upload.parseRequest(request); /* FileItem */
                    StringBuilder sb = new StringBuilder();

                    for (Object item : items) {
                        FileItem fileItem = (FileItem) item;
                        if (!fileItem.isFormField()) {
                            try {
                                List<URL> urls = op.getFeedURLs(fileItem.getInputStream());
                                sb.append("<h2>Uploading " + fileItem.getName() + "</h2>");
                                sb.append("Size: " + fileItem.getSize() + " bytes. <br>");
                                sb.append("Feeds: " + urls.size() + "<br>");
                                Thread t = new FeedEnrollerThread(aardvark, user, urls);
                                t.start();
                            } catch (IOException ioe) {
                                setResponse(response, "OPML Upload Error", "Problem parsing the OPML file: " + ioe.getMessage());
                                sb = null;
                                break;
                            }
                        }
                    }
                    if (sb != null) {
                        setResponse(response, "OPML Upload OK", sb.toString());
                    }
                } catch (AuraException ex) {
                    Logger.getLogger(OpmlUpload.class.getName()).log(Level.SEVERE, null, ex);
                    setResponse(response, "Aura Error", "Can't find user: " + ex.getMessage());
                } catch (FileUploadException ex) {
                    Logger.getLogger(OpmlUpload.class.getName()).log(Level.SEVERE, null, ex);
                    setResponse(response, "Opml Upload Error", "Can't upload: " + ex.getMessage());
                }
            }
        }
    }

    private void setResponse(HttpServletResponse response, String title, String msg) throws IOException {
        PrintWriter out = response.getWriter();
        try {
            out.println("<html>");
            out.println("<head>");
            out.println("<title>" + title + "</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>" + title + "</h1>");
            out.println(msg);
            out.println("</body>");
            out.println("</html>");
        } finally {
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
    }
    // </editor-fold>

    /**
     * Thread that enrols a set of feeds for a user
     */
    class FeedEnrollerThread extends Thread {
        private Aardvark aardvark;
        private User user;
        private List<URL> urls;

        /**
         * Creates the enroller thread
         * @param aardvark the recommendre
         * @param user the user
         * @param urls the set of urls
         */
        public FeedEnrollerThread(Aardvark aardvark, User user, List<URL> urls) {
            this.aardvark = aardvark;
            this.user = user;
            this.urls = urls;
        }

        public void run() {
            for (URL url : urls) {
                try {
                    aardvark.addUserFeed(user, url, Attention.Type.SUBSCRIBED_FEED);
                } catch (AuraException ex) {
                }
            }
        }
    }
}
