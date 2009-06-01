/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.rmi;

import com.sun.labs.aura.datastore.SimilarityConfig;
import com.sun.labs.minion.DocumentVector;
import com.sun.labs.minion.Result;
import com.sun.labs.minion.ResultSet;
import com.sun.labs.minion.SearchEngine;
import com.sun.labs.minion.SearchEngineFactory;
import com.sun.labs.minion.util.Getopt;
import com.sun.labs.minion.util.NanoWatch;
import com.sun.labs.util.LabsLogFormatter;
import com.sun.labs.util.props.ConfigurationManager;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author stgreen
 */
public class DVServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        for(Handler h : Logger.getLogger("").getHandlers()) {
            h.setLevel(Level.ALL);
            h.setFormatter(new LabsLogFormatter());
            try {
                h.setEncoding("utf-8");
            } catch(Exception ex) {
            }
        }
        String flags = "d:c:n:s:";
        Getopt gopt = new Getopt(args, flags);
        String indexDir = null;
        String clientConfig = "clientConfig.xml";
        String serverConfig = "serviceConfig.xml";
        int n = 10000;
        int c;
        while((c = gopt.getopt()) != -1) {
            switch(c) {
                case 'c':
                    clientConfig = gopt.optArg;
                    break;
                case 'd':
                    indexDir = gopt.optArg;
                    break;
                case 's':
                    serverConfig = gopt.optArg;
                    break;
                case 'n':
                    n = Integer.parseInt(gopt.optArg);
                    break;
            }
        }

        SearchEngine e = SearchEngineFactory.getSearchEngine(indexDir);
        ResultSet rs = e.search("aura-type = artist");
        System.out.printf("Got %d results\n", rs.size());
        List<String> l = new ArrayList<String>();
        for(Result r : rs.getResults(0, n)) {
            l.add(r.getKey());
        }

        ConfigurationManager serverCM = null;
        ConfigurationManager clientCM = null;
        try {
            URI serverConfigURI = DVServer.class.getResource(serverConfig).
                    toURI();
            if(serverConfigURI == null) {
                serverConfigURI = (new File(serverConfig)).toURI();
            }
            serverCM = new ConfigurationManager(serverConfigURI.toURL());
            ServerImpl serverImpl = (ServerImpl) serverCM.lookup("server");

            URI clientConfigURI = DVServer.class.getResource(clientConfig).
                    toURI();
            if(clientConfigURI == null) {
                clientConfigURI = (new File(clientConfig)).toURI();
            }
            clientCM = new ConfigurationManager(clientConfigURI.toURL());
            Server server = (Server) clientCM.lookup("server");
            NanoWatch nw = new NanoWatch();
            long call;
            long ret;
            call = 0;
            ret = 0;
            server.reset();
            SimilarityConfig config = new SimilarityConfig("socialTags");
            for(String k : l) {
                DocumentVector dv = e.getDocumentVector(k, "socialTags");
                nw.start();
                long exit = server.dvCall(System.nanoTime(), dv, config);
                ret += System.nanoTime() - exit;
                nw.stop();
            }
            System.out.printf("simple: %d tot: %.3f avg: %.5f\n",
                    n, nw.getTimeMillis(), nw.getAvgTimeMillis());
            call = server.get();
            System.out.printf(" call: %d call avg: %.5f ret: %d ret avg: %.5f\n",
                    call,
                    call / 1000000.0 / n,
                    ret,
                    ret / 1000000.0 / n);
        } finally {
            if(serverCM != null) {
                serverCM.shutdown();
            }
            if(clientCM != null) {
                clientCM.shutdown();
            }
            if(e != null) {
                e.close();
            }
        }

    }
}
