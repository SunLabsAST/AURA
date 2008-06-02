/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.util;

import com.sun.labs.util.props.ComponentRegistry;
import com.sun.labs.util.props.ConfigurationManager;
import com.sun.labs.util.props.PropertyException;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.io.IOException;

/**
 *
 * @author stgreen
 */
public class DumpServices {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        URL cu = DumpServices.class.getResource(args[0]);
        if(cu == null) {
            cu = (new File(args[0])).toURI().toURL();
        }
        listServices(cu);
    }
    
    public static void listServices(URL configURL) throws InterruptedException, PropertyException, IOException {
        ConfigurationManager cm = new ConfigurationManager(configURL);
        ComponentRegistry cr = cm.getComponentRegistry();
        
        if(cr == null) {
            System.out.println("No component registry");
            return;
        }
        Thread.sleep(10000);
        Map<String,List<String>> dump = cr.dumpJiniServices();
        for(Map.Entry<String,List<String>> e : dump.entrySet()) {
            System.out.println("Registrar: " + e.getKey());
            for(String s : e.getValue()) {
                System.out.println("  Service: " + s);
            }
        }
    }
}
