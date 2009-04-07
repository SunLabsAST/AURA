/*
 * Copyright 2007-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
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
