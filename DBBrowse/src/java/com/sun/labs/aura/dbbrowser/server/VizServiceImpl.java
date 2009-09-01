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

package com.sun.labs.aura.dbbrowser.server;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sun.labs.aura.datastore.AttentionConfig;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.impl.PartitionCluster;
import com.sun.labs.aura.datastore.impl.ProcessManager;
import com.sun.labs.aura.datastore.impl.Replicant;
import com.sun.labs.aura.dbbrowser.client.viz.DSHInfo;
import com.sun.labs.aura.dbbrowser.client.viz.PCInfo;
import com.sun.labs.aura.dbbrowser.client.viz.RepInfo;
import com.sun.labs.aura.dbbrowser.client.viz.RepStats;
import com.sun.labs.aura.dbbrowser.client.viz.VizService;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.service.StatService;
import com.sun.labs.util.props.ComponentRegistry;
import com.sun.labs.util.props.ConfigurationManager;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceRegistrar;

/**
 * Implementation of the Viz service, providing visualization info about
 * the data store.
 */
public class VizServiceImpl extends RemoteServiceServlet implements
    VizService {

    protected ConfigurationManager cm;
    protected static Logger logger = Logger.getLogger("");
    
    protected List<ServiceItem> svcs;
    
    protected StatService statService;

    protected boolean statsDown = true;
    
    protected HashMap<String,Replicant> prefixToRep =
            new HashMap<String,Replicant>();
    
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ServletContext context = getServletContext();
        cm = (ConfigurationManager)context.getAttribute("configManager");
        refreshSvcs();
    }

    /**
     * Log the user and host when new sessions start
     * 
     * @param request the request
     * @param response the response
     */
    @Override
    public void service(HttpServletRequest request,
                        HttpServletResponse response)
            throws ServletException, IOException
    {
        HttpSession s = request.getSession();
        if (s.isNew()) {
            logger.info("New session started for "
                    + request.getRemoteUser()
                    + " from " + request.getRemoteHost());
        }
        super.service(request, response);
    }

    public String dump() {
        refreshSvcs();
        String ret = "";
        for (ServiceItem svc : svcs) {
            ret += svc.service.toString() + "<br>";
        }
        return ret;
    }
    
    public void refreshSvcs() {
        ComponentRegistry cr = cm.getComponentRegistry();
        Map<ServiceRegistrar,List<ServiceItem>> reggies = cr.getJiniServices();
        for (ServiceRegistrar sr : reggies.keySet()) {
            logger.fine("ServiceRegistrar: " + sr);
            for (ServiceItem i : reggies.get(sr)) {
                logger.fine("  Service: " + i.toString());
            }
        }
        ServiceRegistrar sr = reggies.keySet().iterator().next();
        svcs = reggies.get(sr);
        statService = null;
        for (ServiceItem svc : svcs) {
            if (svc.service instanceof StatService) {
                statService = (StatService) svc.service;
                break;
            }
        }
    }

    public List getDSHInfo() {
        List ret = new ArrayList();
        for (ServiceItem svc : svcs) {
            if (svc.service instanceof DataStore) {
                DataStore dsh = (DataStore)svc.service;
                //
                // I can't seem to get the IP of the remote client
                // programmatically, but I can get it out of the string... as
                // long as the string doesn't change.
                String desc = svc.toString();
                String start = "endpoint:[";
                String ip = desc.substring(desc.indexOf(start) + start.length(),
                                           desc.length());
                ip = ip.substring(0, ip.indexOf(":"));
                DSHInfo info = newDSHInfo(dsh, ip);
                ret.add(info);
            }
        }
        return ret;
    }
    
    public List getPCInfo() {
        List ret = new ArrayList();
        prefixToRep = new HashMap<String,Replicant>();
        for (ServiceItem svc : svcs) {
            if (svc.service instanceof PartitionCluster) {
                PartitionCluster pc = (PartitionCluster)svc.service;
                PCInfo info = newPCInfo(pc);
                ret.add(info);
            }
        }
        Collections.sort(ret, new Comparator() {
            public int compare(Object o1, Object o2) {
                PCInfo pc1 = (PCInfo)o1;
                PCInfo pc2 = (PCInfo)o2;
                return pc1.getPrefix().compareTo(pc2.getPrefix());
            }
            
        });
        return ret;
    }
    
    public RepStats getRepStats(String prefix) {
        RepStats stats = new RepStats();
        if (statService != null) {
            try {
                for (Replicant.StatName name : Replicant.StatName.values()) {
                    stats.putRate(name.toString(),
                            statService.getAveragePerSecond(
                                repStatName(prefix, name.toString())));
                    Double d = statService.getDouble(repStatName(prefix, name.toString()) + "-time");
                    if (d == null || d.isNaN() || d.isInfinite()) {
                        d = new Double(0);
                    }
                    stats.putTime(name.toString(), d);
                }
                statsDown = false;
            } catch (RemoteException e) {
                if (statsDown == false) {
                    logger.log(Level.WARNING, "Failed to communicate with stats server", e);
                    statsDown = true;
                    throw new RuntimeException("Failed to load stats", e);
                }
            }
        }
        return stats;
    }
    
    public void resetRepStats(String prefix) {
        if (statService != null) {
            try {
                Set<String> prefixes = new HashSet<String>();
                if (prefix != null) {
                    prefixes = Collections.singleton(prefix);
                } else {
                    prefixes = prefixToRep.keySet();
                }
                for (String currPrefix : prefixes) {
                    for (Replicant.StatName name : Replicant.StatName.values()) {
                       statService.set(repStatName(currPrefix, name.toString()), 0);
                       statService.setDouble(repStatName(currPrefix, name.toString()) + "-time", 0);
                    }
                }
                statsDown = false;
            } catch (RemoteException e) {
                if (statsDown == false) {
                    logger.log(Level.WARNING, "Failed to communicate with stats server", e);
                    statsDown = true;
                    throw new RuntimeException("Failed to reset stats");
                }
            }
        }
    }

    public Map<String,Double> getCPULoads() {
        Map<String,Double> result = new HashMap<String,Double>();
        try {
            String[] allStats = statService.getDoubleNames();
            for (String stat : allStats) {
                if (stat.endsWith(ProcessManager.StatName.PERCENT_CPU.toString())) {
                    String src = stat.substring(0, stat.length() -
                            ProcessManager.StatName.PERCENT_CPU.toString().length() - 1);
                    result.put(src, statService.getDouble(stat));
                }
            }
            statsDown = false;
        } catch (RemoteException e) {
            if (statsDown == false) {
                logger.log(Level.WARNING, "Failed to communicate with stats server", e);
                statsDown = true;
            }
        }
        return result;
    }

    /**
     * Gets stats on all registered web servers
     */
    public Map<String,Double> getWebStats() {
        Map <String,Double> result = new HashMap<String,Double>();
        try {
            String[] allStats = statService.getDoubleNames();
            for (String statName : allStats) {
                if (statName.startsWith("web:")) {
                    //
                    // format is web:serverName:statName.  Lop off the "web:"
                    String name = statName.substring(4);
                    result.put(name, statService.getDouble(statName));
                }
            }
            statsDown = false;
        } catch (RemoteException e) {
            if (statsDown == false) {
                logger.log(Level.WARNING, "Failed to communicate with stats server", e);
                statsDown = true;
            }
        }
        return result;
    }

    /**
     * Gets a list of all the available log names for methods in the replicant
     * 
     * @return the list of names
     */
    public List<String> getRepLogNames() {
        ArrayList<String> result = new ArrayList<String>();
        for (Replicant.StatName name : Replicant.StatName.values()) {
            result.add(name.toString());
        }
        return result;
    }
    
    public List<String> getRepSelectedLogNames(String prefix) {
        Replicant rep = prefixToRep.get(prefix);
        try {
            EnumSet<Replicant.StatName> curr = rep.getLoggedStats();
            ArrayList<String> result = new ArrayList<String>();
            for (Replicant.StatName name : curr) {
                result.add(name.toString());
            }
            return result;
        } catch (RemoteException e) {
            logger.log(Level.INFO, "Failed to get selected names", e);
            throw new RuntimeException("Failed to get selected names");
        }
    }
    
    public void setRepSelectedLogNames(String prefix, List<String> selected) {
        EnumSet<Replicant.StatName> names = EnumSet.noneOf(Replicant.StatName.class);
        for (String val : selected) {
            names.add(Replicant.StatName.valueOf(val));
        }

        try {
            if (prefix != null && !prefix.isEmpty()) {
                Replicant rep = prefixToRep.get(prefix);
                rep.setLoggedStats(names);
            } else {
                for (Replicant rep : prefixToRep.values()) {
                    rep.setLoggedStats(names);
                }
            }
        } catch (RemoteException e) {
            logger.log(Level.INFO, "Failed to set selected names", e);
            throw new RuntimeException("Failed to set selected names");
        }
    }
    

    @Override
    public String getLogLevel(String prefix) {
        String ret = "";
        try {
            if (prefix != null && !prefix.isEmpty()) {
                Replicant selected = prefixToRep.get(prefix);
                ret = selected.getLogLevel();
            }
        } catch (RemoteException e) {
            logger.log(Level.INFO, "Failed to get log level", e);
            throw new RuntimeException("Failed to get log level");
        }
        return ret;
    }

    @Override
    public boolean setLogLevel(String prefix, String logLevel) {
        boolean worked = true;
        try {
            if (prefix != null && !prefix.isEmpty()) {
                Replicant selected = prefixToRep.get(prefix);
                worked = selected.setLogLevel(logLevel);
            } else {
                for (Replicant selected : prefixToRep.values()) {
                    if (!selected.setLogLevel(logLevel)) {
                        worked = false;
                    }
                }
            }
        } catch (RemoteException e) {
            logger.log(Level.INFO, "Failed to set log level", e);
            throw new RuntimeException("Failed to set log level");
        }

        return worked;
    }

    public void haltPC(PCInfo pc) {
        logger.info("Halt PC " + pc.getPrefix());
    }
    
    public void splitPC(PCInfo pc) {
        logger.info("Split PC " + pc.getPrefix());
        try {
            for (ServiceItem svc : svcs) {
                if (svc.service instanceof PartitionCluster) {
                    PartitionCluster part = (PartitionCluster)svc.service;
                    if (part.getPrefix().toString().equals(pc.getPrefix())) {
                        part.split();
                    }
                }
            }
        } catch (RemoteException e) {
            logger.log(Level.WARNING,
                    "Failed to initiate split for " + pc.getPrefix(), e);
            throw new RuntimeException("Split failed to start");
        } catch (AuraException e) {
            logger.log(Level.WARNING,
                    "Failed to initiate split for " + pc.getPrefix(), e);
            throw new RuntimeException("Split failed to start");
        }
    }
    
    public void shutDown() {
        try {
            for (ServiceItem svc : svcs) {
                if (svc.service instanceof DataStore) {
                    DataStore dsh = (DataStore)svc.service;
                    dsh.close();
                    break;
                }
            }
        } catch (RemoteException e) {
            logger.log(Level.WARNING,
                       "Failed to shut down datastore", e);
            throw new RuntimeException("Shutdown failed to run");
        } catch (AuraException e) {
            logger.log(Level.WARNING,
                       "Failed to shut down datastore", e);
            throw new RuntimeException("Shutdown failed to run");
        }
    }
    
    /**
     * Factory method for making a DSHInfo from a DSH
     * @param dsh
     * @return
     */
    protected DSHInfo newDSHInfo(DataStore dsh, String ip) {
        DSHInfo ret = new DSHInfo();
        try {
            ret.setIsReady(dsh.ready());
            ret.setIP(ip);
        } catch (RemoteException e) {
            logger.warning("Failed to communicate with DataStoreHead");
        }
        return ret;
    }

    /**
     * Factory method for making a PCInfo from a PC
     * @param pc
     * @return
     */
    protected PCInfo newPCInfo(PartitionCluster pc) {
        PCInfo ret = new PCInfo();
        try {
            ret.setPrefix(pc.getPrefix().toString());
            ret.setNumItems(pc.getItemCount(null));
            ret.setNumAttention(pc.getAttentionCount(new AttentionConfig()));
            Map typeToCount = new HashMap();
            for (Item.ItemType type : Item.ItemType.values()) {
                typeToCount.put(type.toString(), pc.getItemCount(type));
            }
            ret.setTypeToCountMap(typeToCount);
            /*for (ServiceItem svc : svcs) {
                if (svc.service instanceof Replicant) {
                    Replicant rep = (Replicant)svc.service;
                    if (rep.getPrefix().equals(pc.getPrefix())) {
                        ret.addRepInfo(newRepInfo(rep));
                    }
                }
            }*/
            ret.addRepInfo(newRepInfo(pc.getReplicant()));
        } catch (RemoteException e) {
            logger.log(Level.WARNING, "Failed to communicate with partition cluster", e);
            return null;
        } catch (AuraException ex) {
            logger.warning("Aura exception: " + ex.getMessage());
            return null;
        }
        return ret;
    }
    
    /**
     * Factory method for making a RepInfo from a Rep
     * @param rep
     * @return
     */
    protected RepInfo newRepInfo(Replicant rep) {
        RepInfo ret = new RepInfo();
        try {
           ret.setDBSize(rep.getDBSize());
           ret.setIndexSize(rep.getIndexSize());
           String prefix = rep.getPrefix().toString();
           ret.setPrefix(prefix);
           
           prefixToRep.put(prefix, rep);
        } catch (RemoteException e) {
            logger.warning("Failed to get rep info: " + e.getMessage());
        }
        return ret;
    }
    
    private static String repStatName(String prefix, String statName) {
        return "Rep-" + prefix + "-" + statName;
    }
}
