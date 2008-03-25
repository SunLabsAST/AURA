
package com.sun.labs.aura.aardvark.web;

import com.sun.labs.aura.aardvark.Aardvark;
import com.sun.labs.aura.aardvark.web.bean.StatsBean;
import com.sun.labs.aura.util.AuraException;
import java.io.IOException;
import java.rmi.RemoteException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Some shared code across the servlets
 */
public class Shared {

    public static void fillPageHeader(HttpServletRequest req,
                                      Aardvark aardvark)
            throws AuraException, RemoteException {
        StatsBean sb = new StatsBean(aardvark.getStats());
        sb.setNumFeeds(aardvark.getStats().getNumFeeds());
        req.setAttribute("statsBean", sb);
    }
    
    public static void forwardToError(ServletContext context,
                                      HttpServletRequest req,
                                      HttpServletResponse resp,
                                      Exception e) 
            throws ServletException, IOException {
        req.setAttribute("errorMsg", e.getMessage());
        context.getRequestDispatcher("/error.jsp").forward(req, resp);
    }
}
