/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.grid.loadbalance;

import com.sun.caroline.platform.L4VirtualServiceConfiguration;
import com.sun.caroline.platform.Network;
import com.sun.caroline.platform.NetworkAddress;
import com.sun.caroline.platform.NetworkSetting;
import com.sun.caroline.platform.ProcessRegistration;
import com.sun.caroline.platform.RealService;
import com.sun.caroline.platform.Resource;
import com.sun.caroline.platform.ResourceName;
import com.sun.caroline.platform.RunState;
import com.sun.labs.aura.grid.ServiceAdapter;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * A class that can be used to start an HTTP load-balancer on-grid.
 */
public class L4LoadBalancer extends ServiceAdapter {

    /**
     * The (partial) name of the services that we'll be load-balancing.
     */
    @ConfigString(defaultValue = "www")
    public static final String PROP_SERVICE_NAME = "serviceName";
    private String serviceName;
    /**
     * The external host name to use.
     */
    @ConfigString(defaultValue = "")
    public static final String PROP_HOST_NAME = "hostName";
    private String hostName;
    @ConfigString(defaultValue = "http://www.tastekeeper.com/sorry")
    public static final String PROP_SORRY_PAGE = "sorryPage";
    private URI sorryPage;

    public String serviceName() {
        return "StartL4LB";
    }
    private Pattern servicePattern;

    public void start() {

        //
        // Get the services that we'll be balancing.  We'll enumerate all of the
        // network addresses and add the ones that match the pattern of the
        // servlet containers that were deployed.
        List<RealService> services = new ArrayList();
        Network network = gu.getNetwork();
        for (NetworkAddress addr : network.findAllAddresses()) {
            String name = ResourceName.getCSName(addr.getName());
            if (servicePattern.matcher(name).matches()) {

                //
                // See if this network address is associated with a running
                // process.
                try {
                    for (Resource ref : addr.getReferences()) {
                        if (ref instanceof ProcessRegistration) {
                            RunState state = ((ProcessRegistration) ref).getRunState();
                            if (state == RunState.RUNNING ||
                                    state == RunState.STARTING) {
                                logger.info("Got service at " + addr.getName());
                                services.add(new RealService(addr.getUUID(), 80));
                            }
                        }
                    }
                } catch (RemoteException rx) {
                    logger.severe("Error checking network references" + rx);
                }
            }
        }

        logger.info("Got " + services.size() + " services to load balance");

        if (services.size() == 0) {
            return;
        }

        //
        // Now get the configuration.  We'll use the on-grid one if we can.
        NetworkSetting lbns = null;
        L4VirtualServiceConfiguration config = null;
        String lbName = instance + "-lb";


        try {
            lbns = grid.getNetworkSetting(lbName);
            if (lbns != null) {
                config = (L4VirtualServiceConfiguration) lbns.getConfiguration();
            }
        } catch (RemoteException rx) {
            logger.log(Level.SEVERE, "Errory getting network configuration", rx);
        }

        boolean create = config == null;

        //
        // There isn't one, so make one.
        if (create) {
            NetworkAddress ext;
            try {
                //
                // We need an external address and hostname.
                ext = gu.getExternalAddressFor(serviceName, hostName);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Unable to get external address for LB",
                        ex);
                return;
            }
            config = new L4VirtualServiceConfiguration();
            config.setExternalNetworkAddress(ext.getUUID());
        }

        //
        // Set the services to be balanced.
        config.setRealServices(services);

        //
        // Set up the load balancer to balance our servlet containers.
        try {
            if (create) {
                grid.createNetworkSetting(lbName, config);
            } else {
                //
                // Change the actual configuration.
                lbns.changeConfiguration(config);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE,
                    "Error creating or changing load balancer config",
                    ex);
        }
    }

    public void stop() {
    }

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
        serviceName = ps.getString(PROP_SERVICE_NAME);
        hostName = ps.getString(PROP_HOST_NAME);
        if (hostName.trim().length() == 0) {
            hostName = serviceName;
        }
        servicePattern = Pattern.compile(String.format("%s-[0-9]+-int$", serviceName));
        try {
            sorryPage = new URI(ps.getString(PROP_SORRY_PAGE));
        } catch (URISyntaxException ex) {
            throw new PropertyException(ex, ps.getInstanceName(), PROP_SORRY_PAGE, "Bad URI for sorry page: " + ps.getString(
                    PROP_SORRY_PAGE));
        }
    }
}
