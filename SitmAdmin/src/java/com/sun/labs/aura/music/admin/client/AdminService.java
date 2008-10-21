
package com.sun.labs.aura.music.admin.client;
import com.google.gwt.user.client.rpc.RemoteService;
import java.util.List;
import java.util.Map;

/**
 *
 */
public interface AdminService extends RemoteService {
    public Map<String, String> getStatistics() throws AdminException;
    public void addArtist(String mbaid) throws AdminException;
    public void addListener(String userKey) throws AdminException;
    public void addApplication(String applicationID) throws AdminException;
    public List<String> getTests(boolean shortTests) throws AdminException;
    public TestStatus runTest(String test) throws AdminException;
    public List<WorkbenchDescriptor> getWorkerDescriptions() throws AdminException;
    public WorkbenchResult runWorker(String name, Map<String, String> params) throws AdminException;
}
