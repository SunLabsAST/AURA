/*
 * AdminServiceImpl.java
 *
 * Created on February 27, 2008, 1:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.admin.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.AttentionConfig;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.admin.client.AdminException;
import com.sun.labs.aura.music.admin.client.AdminService;
import com.sun.labs.aura.music.admin.client.TestStatus;
import com.sun.labs.aura.music.admin.client.WorkbenchDescriptor;
import com.sun.labs.aura.music.admin.client.WorkbenchResult;
import com.sun.labs.aura.util.AuraException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 */
public class AdminServiceImpl extends RemoteServiceServlet implements
        AdminService {

    protected static MusicDatabase mdb;
    protected static Logger logger = Logger.getLogger("");
    private Random rng = new Random();
    private TestManager testManager = new TestManager();
    private WorkbenchManager workbenchManager = new WorkbenchManager();

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ServletContext context = getServletContext();
        mdb = (MusicDatabase) context.getAttribute("MusicDatabase");
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
            throws ServletException, IOException {
        HttpSession s = request.getSession();
        if (s.isNew()) {
            logger.info("New session started for " + request.getRemoteUser() + " from " + request.getRemoteHost());
        }
        super.service(request, response);
    }
    int tcount = 0;

    public Map<String, String> getStatistics() throws AdminException {

        try {
            DataStore ds = mdb.getDataStore();
            Map<String, String> map = new LinkedHashMap<String, String>();
            map.put("DataStore Ready", Boolean.toString(ds.ready()));
            map.put("DataStore partitions", Integer.toString(ds.getPrefixes().size()));

            for (ItemType type : ItemType.values()) {
                long count = ds.getItemCount(type);
                if (count > 0) {
                    map.put(type.name(), Long.toString(count));
                }
            }

            for (Attention.Type type : Attention.Type.values()) {
                AttentionConfig ac = new AttentionConfig();
                ac.setType(type);
                long count = ds.getAttentionCount(ac);
                if (count > 0) {
                    map.put(type.name() + " attention", Long.toString(count));
                }
            }

            map.put("Last update", new Date().toString());

            return map;
        } catch (AuraException ex) {
            throw new AdminException("getStatistics", "Trouble getting statistics");
        } catch (RemoteException ex) {
            throw new AdminException("getStatistics", "Trouble getting statistics");
        }
    }

    public void addArtist(String mbaid) throws AdminException {
        try {
            if (mdb.artistLookup(mbaid) == null) {
                mdb.addArtist(mbaid);
            } else {
                throw new AdminException("AddArtist", "Artist already exists");
            }
        } catch (AuraException ex) {
            throw new AdminException("Aura Problem", ex.getMessage());
        } catch (RemoteException ex) {
            throw new AdminException("Remote Access Problem", ex.getMessage());
        }
    }

    public void addListener(String userKey) throws AdminException {
        try {
            if (mdb.getListener(userKey) == null) {
                mdb.enrollListener(userKey);
            } else {
                throw new AdminException("AddListener", "Listener already exists");
            }
        } catch (AuraException ex) {
            throw new AdminException("Aura Problem", ex.getMessage());
        } catch (RemoteException ex) {
            throw new AdminException("Remote Access Problem", ex.getMessage());
        }
    }

    public void addApplication(String applicationID) throws AdminException {
        throw new AdminException("AddApplication", "Not implemented");
    }

    public List<String> getTests(boolean shortTests) throws AdminException {
        return testManager.getTestNames(shortTests);
    }

    public TestStatus runTest(String test) throws AdminException {
        return testManager.runTest(test, mdb);
    }

    public List<WorkbenchDescriptor> getWorkerDescriptions() throws AdminException {
        return workbenchManager.getWorkbenchDescriptors();
    }

    public WorkbenchResult runWorker(String name, Map<String, String> params) throws AdminException {
        return workbenchManager.runWorker(mdb, name, params);
    }
}
