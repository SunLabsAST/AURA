/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.website;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Web application lifecycle listener.
 * @author ja151348
 */

public class ServletListener implements ServletContextListener {

    public void contextInitialized(ServletContextEvent sce) {
        //
        // Open an output file and store it in the context
        ServletContext context = sce.getServletContext();
        String path = context.getInitParameter("survey.out");

        if (path != null) {
            File f = new File(path);
            try {
                PrintWriter pw = new PrintWriter(new FileOutputStream(f, true));
                context.setAttribute("surveyWriter", pw);
            } catch (FileNotFoundException e) {
                Logger.getLogger("").log(Level.SEVERE,
                        "Couldn't open survey output: " + path, e);
            }
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
        PrintWriter pw = (PrintWriter) sce.getServletContext().getAttribute("surveyWriter");
        if (pw != null) {
            pw.close();
        }
    }
}