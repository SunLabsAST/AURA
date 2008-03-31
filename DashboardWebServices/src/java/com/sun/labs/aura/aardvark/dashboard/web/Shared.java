
package com.sun.labs.aura.aardvark.dashboard.web;

import java.io.IOException;
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
                                      HttpServletResponse resp,
                                      String msg) 
            throws ServletException, IOException {
        req.setAttribute("errorMsg", msg);
        context.getRequestDispatcher("/error.jsp").forward(req, resp);
    }
}
