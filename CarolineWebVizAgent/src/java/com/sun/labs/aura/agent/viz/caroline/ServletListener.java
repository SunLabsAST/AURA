/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.agent.viz.caroline;

import com.sun.caroline.platform.DestroyInProgressException;
import com.sun.caroline.platform.Grid;
import com.sun.caroline.platform.GridFactory;
import com.sun.caroline.platform.IncarnationMismatchException;
import com.sun.caroline.platform.ProcessContext;
import com.sun.caroline.platform.ProcessRegistration;
import com.sun.caroline.platform.ProcessRegistration.Metrics;
import com.sun.caroline.platform.ProcessRegistration.ThreadMetrics;
import com.sun.caroline.platform.RunStateException;
import com.sun.labs.aura.service.StatService;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.RemoteComponentManager;
import com.sun.labs.util.props.ConfigurationManager;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
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
        Logger logger = Logger.getLogger("");
        try {
            final ServletContext context = sce.getServletContext();
            URL config = context.getResource("/agentWebConfig.xml");

            //
            // Get the stat service
            try {
                ConfigurationManager cm = new ConfigurationManager();
                cm.addProperties(config);
                RemoteComponentManager rcm = new RemoteComponentManager(cm, StatService.class);
                rcm.getComponent();
                context.setAttribute("rcm", rcm);
                logger.info("Found all components");

                //
                // Get the grid and this process reg
                ProcessContext proc = GridFactory.getProcessContext();
                if (proc != null) {
                    logger.info("Running on grid, will track stats");
                    Grid grid = proc.getGrid();
                    UUID regUUID = proc.getProcessRegistrationUUID();
                    ProcessRegistration reg = null;
                    String regName = null;
                    try {
                        reg = (ProcessRegistration)grid.getResource(regUUID);
                        context.setAttribute("reg", reg);
                        regName = reg.getName().substring(reg.getName().indexOf(':') + 1);
                        context.setAttribute("regName", regName);
                    } catch (RemoteException e) {
                        logger.log(Level.SEVERE, "Failed to get process registration", e);
                    }

                    if (reg != null) {
                        TimerTask updateStats = new TimerTask() {
                            @Override
                            public void run() {
                                sendStats(context);
                            }
                        };
                        Timer timer = new Timer();
                        timer.schedule(updateStats, 0, 1000 * 10);
                        context.setAttribute("timer", timer);
                    }
                }

            } catch (IOException ioe) {
                logger.log(Level.SEVERE, "Failed to get service handle", ioe);
            } catch (AuraException e) {
                logger.log(Level.SEVERE, "Failed to get stat service", e);
            }
        } catch (MalformedURLException ex) {
            logger.severe("Bad URL to config file " + ex.getMessage());
        }

    }

    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        Timer timer = (Timer)context.getAttribute("timer");
        timer.cancel();

        //
        // Clear the stats
        RemoteComponentManager rcm = (RemoteComponentManager)context.getAttribute("rcm");
        if (rcm != null) {
            rcm.shutdown();
        }
    }

    protected static StatService getStatService(ServletContext context) throws AuraException {
        RemoteComponentManager rcm = (RemoteComponentManager)context.getAttribute("rcm");
        return (StatService)rcm.getComponent();
    }

    protected static void sendStats(ServletContext context) {
        Logger logger = Logger.getLogger("");
        try {
            //
            // Get the CPU usage for this instance
            ProcessRegistration reg = (ProcessRegistration)context.getAttribute("reg");
            Metrics proc = reg.getMetrics(true);
            float totalLoad = 0;
            for (ThreadMetrics thread : proc.getThreadMetrics()) {
                totalLoad += thread.getPercentCpu();
            }
            getStatService(context).setDouble(statPrefix(context) + "PERCENT_CPU", totalLoad);

            //
            // Get the number of active sessions in the root context
            ServletContext rootContext = context.getContext("/");
            if (rootContext != null) {
                Integer numActiveSessions = (Integer)rootContext.getAttribute("numActiveSessions");
                if (numActiveSessions != null) {
                    getStatService(context).setDouble(statPrefix(context) + "ACTIVE_SESSIONS", numActiveSessions.doubleValue());
                }
            } else {
                logger.info("Failed to get root context");
            }
        } catch (RemoteException e) {
            logger.log(Level.WARNING, "Communication failure", e);
        } catch (DestroyInProgressException e) {
            logger.log(Level.WARNING, "Grid related failure", e);
        } catch (IncarnationMismatchException e) {
            logger.log(Level.WARNING, "Grid related failure", e);
        } catch (RunStateException e) {
            logger.log(Level.WARNING, "Grid related failure", e);
        } catch (AuraException e) {
            logger.log(Level.WARNING, "Failed to send stat", e);
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Something is busted up!", t);
        }
    }

    protected static String statPrefix(ServletContext context) {
        String regName = (String)context.getAttribute("regName");
        return "web:" + regName + ":";
    }
}