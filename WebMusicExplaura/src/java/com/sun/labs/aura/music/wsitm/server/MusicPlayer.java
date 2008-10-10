/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.wsitm.server;

import com.sun.labs.aura.music.MusicDatabase;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author plamere
 */
public class MusicPlayer extends HttpServlet {

    private MusicDatabase mdb;
    private String playerString = null;

    @Override
    public void init(ServletConfig sc) throws ServletException {
        super.init(sc);
        mdb = ServletTools.getMusicDatabase(sc);
        loadPlayerString();
    }

    private void loadPlayerString() {
        try {
            StringBuilder sb = new StringBuilder();
            InputStream is = MusicPlayer.class.getResourceAsStream("Player.html");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = null;

            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
            reader.close();
            playerString = sb.toString();
        } catch (IOException ioe) {
            playerString = "<body> <h2> Error loading player data</h2></body>";
        }
    }

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        String name = request.getParameter("name");
        if (name == null) {
            name="barry+manilow"; //hehe
        }

        String type = request.getParameter("type");
        if (type == null) {
            type="artist"; //hehe
        }

        PrintWriter out = response.getWriter();
        try {
            String outputString = playerString.replaceAll("__NAME__", name);
            outputString = outputString.replaceAll("__TYPE__", type);
            out.println(outputString);
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
        return "a music player";
    }// </editor-fold>
}
