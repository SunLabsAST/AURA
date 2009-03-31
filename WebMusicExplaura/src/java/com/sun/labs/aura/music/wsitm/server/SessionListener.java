
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