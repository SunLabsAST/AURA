
package com.sun.labs.aura.music.webservices;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Some shared code across the servlets
 */
public class Shared {

    
    public static void forwardToError(ServletContext context,
                                      HttpServletRequest req,
                                      HttpServletResponse resp,
                                      Exception e) 
            throws ServletException, IOException {
        forwardToError(context, req, resp, e.getMessage());
    }

    public static void forwardToError(ServletContext context,
                                      HttpServletRequest req,
                                      HttpServletResponse response,
                                      String msg) 
            throws ServletException, IOException {
            req.setAttribute("errorMsg", msg);
            PrintWriter out = response.getWriter();
            response.setContentType("text/html;charset=UTF-8");
            out.println("<html>");
            out.println("<head>");
            out.println("<title> Error" + msg + "</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h2>" + "Error " + msg + "</h2>");
            out.println("</body>");
            out.println("</html>");
            out.close();
    }
}
