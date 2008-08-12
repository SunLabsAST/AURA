/*
 * VizServiceImpl.java
 *
 * Created on July 18, 2008, 1:45 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.dbbrowser.server;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sun.labs.aura.datastore.AttentionConfig;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.impl.PartitionCluster;
import com.sun.labs.aura.datastore.impl.Replicant;
import com.sun.labs.aura.dbbrowser.client.DSHInfo;
import com.sun.labs.aura.dbbrowser.client.PCInfo;
import com.sun.labs.aura.dbbrowser.client.RepInfo;
import com.sun.labs.aura.dbbrowser.client.VizService;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.util.props.ComponentRegistry;
import com.sun.labs.util.props.ConfigurationManager;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
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
    
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ServletContext context = getServletContext();
        cm = (ConfigurationManager)context.getAttribute("configManager");
        refreshSvcs();
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
            logger.info("ServiceRegistrar: " + sr);
            for (ServiceItem i : reggies.get(sr)) {
                logger.info ("  Service: " + i.toString());
            }
        }
        ServiceRegistrar sr = reggies.keySet().iterator().next();
        svcs = reggies.get(sr);
    }

    public List getDSHInfo() {
        List ret = new ArrayList();
        for (ServiceItem svc : svcs) {
            if (svc.service instanceof DataStore) {
                DataStore dsh = (DataStore)svc.service;
                DSHInfo info = newDSHInfo(dsh);
                ret.add(info);
            }
        }
        return ret;
    }
    
    public List getPCInfo() {
        List ret = new ArrayList();
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
    protected DSHInfo newDSHInfo(DataStore dsh) {
        DSHInfo ret = new DSHInfo();
        try {
            ret.setIsReady(dsh.ready());
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
        } catch (RemoteException e) {
            logger.warning("Failed to get rep info: " + e.getMessage());
        }
        return ret;
    }
}
