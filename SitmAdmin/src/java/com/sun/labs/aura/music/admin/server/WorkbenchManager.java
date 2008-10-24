/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */

package com.sun.labs.aura.music.admin.server;

import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.admin.client.WorkbenchDescriptor;
import com.sun.labs.aura.music.admin.client.WorkbenchResult;
import com.sun.labs.aura.music.admin.client.Constants.Bool;
import com.sun.labs.aura.util.AuraException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author plamere
 */
public class WorkbenchManager {
    private Map<String, Worker> workers;

    WorkbenchManager() {
        workers = new LinkedHashMap<String, Worker>();
        addWorkers();
    }

    private void addWorkers() {
        addWorker(new ArtistFindSimilarWorker());
        addWorker(new ArtistTagFindSimilar());
        addWorker(new ArtistJourney());
        addWorker(new ArtistSearchWorker());
        addWorker(new ArtistSummary());
        addWorker(new ArtistTagSummary());
        addWorker(new HubFinder());
        addWorker(new ListenerSummary());
        addWorker(new MissingPhotoWorker());
        addWorker(new OrphanCleanup());
        addWorker(new ShowItem());
        addWorker(new ShowRecentAttention());
    }

    private void addWorker(Worker w) {
        workers.put(w.getDescriptor().getName(), w);
    }


    public List<WorkbenchDescriptor> getWorkbenchDescriptors() {
        List<WorkbenchDescriptor> descriptors = new ArrayList<WorkbenchDescriptor>();

        for (Worker worker : workers.values()) {
            descriptors.add(worker.getDescriptor());
        }
        return descriptors;
    }
    
    public WorkbenchResult runWorker(MusicDatabase mdb, String cmd, Map<String, String> parameters) {
        Worker worker = workers.get(cmd);
        if (worker == null) {
            return new WorkbenchResult(false, "Unknown command " + cmd);
        } else {
            return worker.runWorker(mdb, parameters);
        }
    }
}

class SampleWorker extends Worker {
    
    SampleWorker(String name) {
        super(name, "A test worker");
        param("loops", "The number of loops to run", 10);
        param("error", "if 'true' generate an error", Bool.values(), Bool.FALSE);
    }

    @Override
    void go(MusicDatabase mdb, Map<String, String> params, WorkbenchResult result) throws AuraException, RemoteException {
        int loops = getParamAsInt(params, "loops");
        boolean error = getParamAsEnum(params, "error") == Bool.TRUE;
        
        if (error) {
            result.fail("That's an error");
            return;
        }

        for (int i = 0; i < loops; i++) {
            result.output("this is loop " + i);
        }
    }
}
