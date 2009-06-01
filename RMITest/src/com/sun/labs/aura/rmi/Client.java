/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.rmi;

import com.sun.labs.minion.util.Getopt;
import com.sun.labs.minion.util.NanoWatch;
import com.sun.labs.util.props.ConfigurationManager;
import java.io.File;
import java.net.URI;

/**
 *
 * @author stgreen
 */
public class Client {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        String flags = "c:n:";
        Getopt gopt = new Getopt(args, flags);
        String config = "clientConfig.xml";
        int n = 10000;
        int c;
        while((c = gopt.getopt()) != -1) {
            switch(c) {
                case 'c':
                    config = gopt.optArg;
                    break;
                case 'n':
                    n = Integer.parseInt(gopt.optArg);
                    break;
            }
        }

        ConfigurationManager cm = null;
        try {
            URI configURI = Client.class.getResource(config).toURI();
            if(configURI == null) {
                configURI = (new File(config)).toURI();
            }
            cm = new ConfigurationManager(configURI.toURL());
            Server server = (Server) cm.lookup("server");
            System.out.println(String.format("server: %s", server));
            NanoWatch nw = new NanoWatch();
            for(int i = 0; i < n; i++) {
                nw.start();
                long ret = server.simple(System.nanoTime());
                nw.stop();
            }
            System.out.printf("simple: %d tot: %.3f avg: %.5f\n",
                    n, nw.getTimeMillis(), nw.getAvgTimeMillis());
            nw.reset();
            for(int i = 0; i < n; i++) {
                nw.start();
                long ret = server.intCall(System.nanoTime(), i);
                nw.stop();
            }
            System.out.printf("intCall: %d tot: %.3f avg: %.5f\n",
                    n, nw.getTimeMillis(), nw.getAvgTimeMillis());
            for(int i = 0; i < n; i++) {
                nw.start();
                long ret = server.stringCall(System.nanoTime(), "Stephen Green");
                nw.stop();
            }
            System.out.printf("intCall: %d tot: %.3f avg: %.5f\n",
                    n, nw.getTimeMillis(), nw.getAvgTimeMillis());
        } finally {
            if(cm != null) {
                cm.shutdown();
            }
        }

    }

}
