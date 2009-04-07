/*
 * Copyright 2008-2009 Sun Microsystems, Inc. All Rights Reserved.
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

    WorkbenchManager(MusicDatabase mdb) {
        workers = new LinkedHashMap<String, Worker>();
        addWorkers(mdb);
    }

    private void addWorkers(MusicDatabase mdb) {
        addWorker(new ArtistGetAll());
        addWorker(new ArtistFindSimilarWorker());
        addWorker(new ArtistTagFindSimilar());
        addWorker(new ArtistJourney());
        addWorker(new ArtistSearchWorker());
        addWorker(new ArtistSummary());
        addWorker(new ArtistTagSummary());
        addWorker(new AttentionCleanup());
        addWorker(new GeneralSearch());
        addWorker(new HubFinder());
        addWorker(new ListenerGetAttended());
        addWorker(new ListenerSummary());
        addWorker(new MissingPhotoWorker());
        addWorker(new OrphanCleanup());
        addWorker(new RecommendationWorker(mdb));
        addWorker(new RemoveUser());
        addWorker(new RemoveAllUsers());
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
