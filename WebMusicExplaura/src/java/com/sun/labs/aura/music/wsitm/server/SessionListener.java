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

package com.sun.labs.aura.music.wsitm.server;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Web application lifecycle listener.
 * @author ja151348
 */

public class SessionListener implements HttpSessionListener {
    protected AtomicInteger numActiveSessions = new AtomicInteger(0);
    protected ServletContext context = null;
    protected Timer timer = new Timer();
    protected Logger logger = Logger.getLogger("");

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        numActiveSessions.incrementAndGet();

        if (context == null) {
            //
            // This is the first session to be created
            context = session.getServletContext();
            TimerTask countUpdater = new TimerTask() {
                @Override
                public void run() {
                    try {
                        context.setAttribute("numActiveSessions", numActiveSessions.get());
                    } catch (Throwable t) {
                        logger.log(Level.WARNING, "Session counter is busted", t);
                        timer.cancel();
                    }
                }
            };
            //
            // Update the number of active sessions every 5 seconds
            timer.schedule(countUpdater, new Date(), 1000 * 5);
        }
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        numActiveSessions.decrementAndGet();
    }
}
