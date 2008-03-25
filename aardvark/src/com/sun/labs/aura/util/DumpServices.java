/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.util;

import com.sun.labs.util.props.ComponentRegistry;
import com.sun.labs.util.props.ConfigurationManager;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 *
 * @author stgreen
 */
public class DumpServices {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

        ConfigurationManager cm = new ConfigurationManager((new File(args[0])).toURI().toURL());
        ComponentRegistry cr = cm.getComponentRegistry();
        
        Thread.sleep(10000);
        if(cr == null) {
            System.out.println("No component registry");
            return;
        }
        Map<String,List<String>> dump = cr.dumpJiniServices();
        for(Map.Entry<String,List<String>> e : dump.entrySet()) {
            System.out.println("Registrar: " + e.getKey());
            for(String s : e.getValue()) {
                System.out.println("  Service: " + s);
            }
        }
        // TODO code application logic here
    }

}
