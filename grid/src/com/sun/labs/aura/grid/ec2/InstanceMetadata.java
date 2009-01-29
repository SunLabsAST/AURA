package com.sun.labs.aura.grid.ec2;

import java.io.InputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class to manage instance metadata on the EC2 grid.  This will probably
 * only do something useful on the EC2 grid.
 */
public class InstanceMetadata {

    private String mdURI = "http://169.254.169.254/latest/user-data";
    private Map<String,String> md;
    private Logger logger = Logger.getLogger(InstanceMetadata.class.getName());

    public InstanceMetadata() {
        try {
            URI u = new URI(mdURI);
            InputStream is = u.toURL().openStream();
            Properties props = new Properties();
            props.load(is);
            is.close();
            for(Map.Entry<Object,Object> e : props.entrySet()) {
                md.put((String) e.getKey(), (String) e.getValue());
            }
        } catch (java.net.URISyntaxException urie) {
            logger.severe("Bad metadata URI: " + mdURI);
        } catch (java.net.MalformedURLException male) {
            logger.severe("Malformed metadata URI: " + mdURI);
        } catch (java.io.IOException ioe) {
            logger.log(Level.SEVERE, "Error reading instance metadata", ioe);
        }
    }

    public String get(String key) {
        return md.get(key);
    }

    public Set<String> keySet() {
        return new HashSet<String>(md.keySet());
    }
}
