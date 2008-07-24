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
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.impl.PartitionCluster;
import com.sun.labs.aura.datastore.impl.Replicant;
import com.sun.labs.aura.dbbrowser.client.DSHInfo;
import com.sun.labs.aura.dbbrowser.client.PCInfo;
import com.sun.labs.aura.dbbrowser.client.RepInfo;
import com.sun.labs.aura.dbbrowser.client.VizService;
import com.sun.labs.util.props.ComponentRegistry;
import com.sun.labs.util.props.ConfigurationManager;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    protected static DataStore store;
    protected ConfigurationManager cm;
    protected static Logger logger = Logger.getLogger("");
    
    protected List<ServiceItem> svcs;
    
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ServletContext context = getServletContext();
        store = (DataStore)context.getAttribute("dataStore");
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
    
    protected void refreshSvcs() {
        ComponentRegistry cr = cm.getComponentRegistry();
        Map<ServiceRegistrar,List<ServiceItem>> reggies = cr.getJiniServices();
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
        return ret;
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
            for (ServiceItem svc : svcs) {
                if (svc.service instanceof Replicant) {
                    Replicant rep = (Replicant)svc.service;
                    if (rep.getPrefix().equals(pc.getPrefix())) {
                        ret.addRepInfo(newRepInfo(rep));
                    }
                }
            }
        } catch (RemoteException e) {
            logger.warning("Failed to communicate with partition cluster");
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
        return ret;
    }
}
