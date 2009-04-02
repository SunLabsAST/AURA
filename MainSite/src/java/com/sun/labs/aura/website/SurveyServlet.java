
package com.sun.labs.aura.website;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Handles survey submission
 * @author ja151348
 */
public class SurveyServlet extends HttpServlet {


    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        String servletPath = request.getServletPath();
        HttpSession session = request.getSession();
        RequestDispatcher rd = request.getRequestDispatcher("/wmesurvey.jsp");
        Boolean alreadyTaken = (Boolean)session.getAttribute("alreadyTaken");
        if (alreadyTaken != null && alreadyTaken == true) {
            request.setAttribute("alreadyTaken", true);
            request.setAttribute("submitted", false);
        } else {
            request.setAttribute("alreadyTaken", false);

            if (servletPath.equals("/wmesurveysubmit")) {
                session.setAttribute("alreadyTaken", true);
                request.setAttribute("submitted", true);
                request.setAttribute("alreadyTaken", true);
                //
                // Get all the values and store them to a file
                PrintWriter pw = (PrintWriter) session.getServletContext().getAttribute("surveyWriter");
                synchronized(pw) {
                    pw.print(request.getParameter("question1") + "\t");
                    pw.print(request.getParameter("question2") + "\t");
                    pw.print(request.getParameter("question3") + "\t");
                    pw.print(request.getParameter("question4") + "\t");
                    pw.print(request.getParameter("question5") + "\t");
                    pw.print(request.getParameter("question6") + "\t");
                    pw.print(request.getParameter("question7") + "\t");
                    pw.print(request.getParameter("question8") + "\t");
                    pw.print(request.getParameter("question9").replaceAll("[\\r\\n\\t]", " ") + "\t");
                    pw.println(request.getParameter("question10".replaceAll("[\\r\\n\\t]", " ")));
                    pw.flush();
                }
            }
        }
        rd.forward(request, response);
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Survey Servlet";
    }// </editor-fold>

}
