/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.rmi;

import com.sun.labs.minion.util.Getopt;
import com.sun.labs.minion.util.NanoWatch;
import com.sun.labs.util.SimpleLabsLogFormatter;
import com.sun.labs.util.props.ConfigurationManager;
import java.io.File;
import java.net.URI;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author stgreen
 */
public class ClientServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        for(Handler h : Logger.getLogger("").getHandlers()) {
            h.setLevel(Level.ALL);
            h.setFormatter(new SimpleLabsLogFormatter());
            try {
                h.setEncoding("utf-8");
            } catch(Exception ex) {
            }
        }
        String flags = "c:n:s:";
        Getopt gopt = new Getopt(args, flags);
        String clientConfig = "clientConfig.xml";
        String serverConfig = "serviceConfig.xml";
        int n = 10000;
        int c;
        while((c = gopt.getopt()) != -1) {
            switch(c) {
                case 'c':
                    clientConfig = gopt.optArg;
                    break;
                case 's':
                    serverConfig = gopt.optArg;
                    break;
                case 'n':
                    n = Integer.parseInt(gopt.optArg);
                    break;
            }
        }

        ConfigurationManager serverCM = null;
        ConfigurationManager clientCM = null;
        try {
            URI serverConfigURI = ClientServer.class.getResource(serverConfig).
                    toURI();
            if(serverConfigURI == null) {
                serverConfigURI = (new File(serverConfig)).toURI();
            }
            serverCM = new ConfigurationManager(serverConfigURI.toURL());
            ServerImpl serverImpl = (ServerImpl) serverCM.lookup("server");

            URI clientConfigURI = ClientServer.class.getResource(clientConfig).
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
            for(int i = 0; i < n; i++) {
                nw.start();
                long exit = server.simple(System.nanoTime());
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
            nw.reset();
            server.reset();
            ret = 0;
            for(int i = 0; i < n; i++) {
                nw.start();
                long exit = server.intCall(System.nanoTime(), i);
                ret += System.nanoTime() - exit;
                nw.stop();
            }
            System.out.printf("intCall: %d tot: %.3f avg: %.5f\n",
                    n, nw.getTimeMillis(), nw.getAvgTimeMillis());
            call = server.get();
            System.out.printf(" call: %d call avg: %.5f ret: %d ret avg: %.5f\n",
                    call,
                    call / 1000000.0 / n,
                    ret,
                    ret / 1000000.0 / n);
            nw.reset();
            server.reset();
            ret = 0;
            for(int i = 0; i < n; i++) {
                nw.start();
                long exit =
                        server.stringCall(System.nanoTime(), "Stephen Green");
                ret += System.nanoTime() - exit;
                nw.stop();
            }
            System.out.printf("stringCall: %d tot: %.3f avg: %.5f\n",
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
        }

    }
}
