package com.sun.labs.aura.grid.ec2;

import com.sun.labs.minion.util.StopWatch;
import com.xerox.amazonws.ec2.AttachmentInfo;
import com.xerox.amazonws.ec2.EC2Exception;
import com.xerox.amazonws.ec2.InstanceType;
import com.xerox.amazonws.ec2.Jec2;
import com.xerox.amazonws.ec2.KeyPairInfo;
import com.xerox.amazonws.ec2.LaunchConfiguration;
import com.xerox.amazonws.ec2.ReservationDescription;
import com.xerox.amazonws.ec2.VolumeInfo;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *  A utility class for an EC2 grid.
 */
public class EC2Grid {

    private Jec2 ec2;

    private Logger logger = Logger.getLogger(EC2Grid.class.getName());

    private Properties props;

    private List<VolumeInfo> dataVols;

    private VolumeInfo distVol;

    private VolumeInfo crawlerVol;

    private VolumeInfo loginVol;

    public EC2Grid() throws IOException {
        props = new Properties();
        props.load(Ec2Sample.class.getResourceAsStream("aws.properties"));
        init(props);
    }

    public EC2Grid(String propertiesFile) throws IOException {
        this(new File(propertiesFile));
    }

    public EC2Grid(File propertiesFile) throws IOException {
        props = new Properties();
        InputStream is = new FileInputStream(propertiesFile);
        props.load(is);
        is.close();
        init(props);
    }

    public EC2Grid(Properties props) {
        this.props = new Properties(props);
        init(props);
    }

    private void init(Properties props) {
        ec2 = new Jec2(props.getProperty("aws.accessId"),
                props.getProperty("aws.secretKey"));

        //
        // Get information on the volumes.  These will need to be set up
        // out-of band.
        try {
            dataVols = ec2.describeVolumes(new String[]{
                        props.getProperty("volume.data.00"),
                        props.getProperty("volume.data.01"),
                        props.getProperty("volume.data.10"),
                        props.getProperty("volume.data.11")});
            distVol = ec2.describeVolumes(new String[] {
                        props.getProperty("volume.dist")}).get(0);
            crawlerVol = ec2.describeVolumes(new String[] {
                        props.getProperty("volume.crawler")}).get(0);
            loginVol = ec2.describeVolumes(new String[] {
                        props.getProperty("volume.login")}).get(0);
        } catch (EC2Exception ex) {
        }
    }

    /**
     * Gets the key pair information associated with a given key pair name,
     * creating it if necessary.  The resulting key material will be written
     * into a file in ~/.ec2 named for the key, so that it may be used later for
     * sshing into instances.
     * 
     * @param keyPairName the name of the key pair to get or generate.
     * @return the key pair info associated with the name, or <code>null</code>
     * if there is any error getting the info
     */
    public KeyPairInfo getKeyPairInfo(String keyPairName) {
        KeyPairInfo kpi = null;
        try {
            List<KeyPairInfo> kpis = ec2.describeKeyPairs(new String[]{
                        keyPairName});
            kpi = kpis.get(0);
        } catch(EC2Exception ecex) {
            try {
                kpi = ec2.createKeyPair(keyPairName);

                String fileName = System.getProperty("user.home") +
                        File.separatorChar +
                        ".ec2" +
                        File.separatorChar + "id_rsa-" +
                        keyPairName;
                try {
                    //
                    // Write the key pair to a file in ~/.ec2 so that we can find it later.
                    BufferedWriter w = new BufferedWriter(new OutputStreamWriter(
                            new FileOutputStream(fileName)));
                    w.append(kpi.getKeyMaterial());
                    w.close();
                } catch (IOException ioe) {
                    logger.warning(String.format("Unable to write key pair to %s", fileName));
                }
            } catch(EC2Exception ecex2) {
                logger.severe(String.format("Unable to get key pair information describe error " +
                        ecex + " create error " + ecex2));
            }
        }
        return kpi;
    }

    public VolumeInfo getDistVol() {
        return distVol;
    }

    public List<VolumeInfo> getVolumeInfo() {
        try {
            return ec2.describeVolumes(new String[0]);
        } catch(EC2Exception ex) {
            logger.warning("Exception getting volume information");
            return new ArrayList<VolumeInfo>();
        }
    }

    /**
     * Gets the information for an EBS volume, creating one if necessary.
     *
     * @param volumeID the ID of the volume to fetch.
     * @return the information for the named volume, or <code>null</code> if
     * there is no such volume defined.
     */
    public VolumeInfo getVolumeInfo(String volumeID) {
        try {
            for(VolumeInfo info : ec2.describeVolumes(new String[0])) {
                if(info.getVolumeId().equals(volumeID)) {
                    return info;
                }
            }
        } catch(EC2Exception ex) {
            logger.warning("Exception getting volume information");
        }
        return null;
    }

    /**
     * Creates a volume with the given parameters.
     *
     * @param size the size of the volume to create, in GB.
     * @param zoneName the name of the availability zone in which the volume
     * should be created.
     * @return the information for the volume that was created, or <code>null</code>
     * if there was an error creating the volume.
     */
    public VolumeInfo createVolume(int size, String zoneName) {
        try {
            return ec2.createVolume(String.valueOf(size), null, zoneName);
        } catch(EC2Exception ex) {
            logger.log(Level.SEVERE, "Error creating volume", ex);
        }
        return null;
    }

    /**
     * Gets the instances that were started from a particular AMI.
     */
    public List<ReservationDescription.Instance> getInstances(String ami) {
        List<ReservationDescription.Instance> ret =
                new ArrayList<ReservationDescription.Instance>();
        try {
            for(ReservationDescription rd : ec2.describeInstances(new String[0])) {
                for(ReservationDescription.Instance inst : rd.getInstances()) {
                    if(inst.isRunning() && inst.getImageId().equals(ami)) {
                        ret.add(inst);
                    }
                }
            }
        } catch(EC2Exception ex) {
            logger.log(Level.WARNING, "Error getting instance information", ex);
        }
        return ret;
    }

    public String getProperty(String key) {
        return props.getProperty(key);
    }

    public void setProperty(String key, String value) {
        props.setProperty(key, value);
    }

    public Properties getProperties() {
        Properties rep = new Properties();
        for(String key : props.stringPropertyNames()) {
            rep.setProperty(key, props.getProperty(key));
        }
        return rep;
    }

    /**
     * Launches an instance with a given AMI.
     */
    public ReservationDescription.Instance launch(String ami, InstanceType type, KeyPairInfo kpi) throws EC2Exception {
        return launch(ami, type, kpi, null, null);
    }

    public ReservationDescription.Instance launch(String ami, InstanceType type, KeyPairInfo kpi, String userData) throws EC2Exception {
        return launch(ami, type, kpi, userData, null);
    }

    public ReservationDescription.Instance launch(String ami, InstanceType type, KeyPairInfo kpi, String userData,VolumeInfo vol) throws EC2Exception {
           return launch(ami, type, kpi, userData, vol, 2);
    }
    
    public ReservationDescription.Instance launch(String ami, InstanceType type, KeyPairInfo kpi, String userData, VolumeInfo vol, int device) throws EC2Exception {
        LaunchConfiguration lc = new LaunchConfiguration(ami);
        if(type != null) {
            lc.setInstanceType(type);
        }
        lc.setKeyName(kpi.getKeyName());
        if(userData != null && userData.length() > 0) {
            lc.setUserData(userData.getBytes());
        }
        if(vol != null) {
            lc.setAvailabilityZone(vol.getZone());
        }
        ReservationDescription rd = ec2.runInstances(lc);
        ReservationDescription.Instance inst = rd.getInstances().get(0);
        String[] instanceID = new String[] {inst.getInstanceId()};
        if(vol != null) {

            StopWatch sw = new StopWatch();
            sw.start();
            //
            // Wait until the instance is running and then attach the volume.
            int retries = 0;
            while(!inst.isRunning() && retries < 120) {
                try {
                    Thread.sleep(2000);
                } catch(InterruptedException ex) {
                }
                inst = ec2.describeInstances(instanceID).get(0).getInstances().get(0);
            }
            sw.stop();
            logger.info(String.format(inst.getInstanceId() + " is " + inst.getState() + " took " + sw.getTime()));
            if(inst.isRunning()) {
                ec2.attachVolume(vol.getVolumeId(), inst.getInstanceId(), String.valueOf(device));
            } else {
                logger.warning("Launched instance " + 
                        inst.getInstanceId() +
                        " but couldn't attach volume");
                throw new EC2Exception("Could not atttach volume " + 
                        vol + " to instance " + inst.getInstanceId());
            }
        }
        return inst;
    }

    public ReservationDescription.Instance getInstance(String instanceID) {
        try {
            List<ReservationDescription> l =
                    ec2.describeInstances(new String[]{instanceID});
            if(l == null || l.size() == 0) {
                return null;
            }
            return l.get(0).getInstances().get(0);
        } catch(EC2Exception ex) {
            logger.log(Level.WARNING, "Error getting instance for ID " + instanceID, ex);
            return null;
        }
    }

    public AttachmentInfo getAttachmentInfo(VolumeInfo vi) {
        List<AttachmentInfo> l = vi.getAttachmentInfo();
        if(l == null || l.size() == 0) {
            return null;
        }
        return l.get(0);
    }

    public void attachVolume(ReservationDescription.Instance inst, VolumeInfo vol) throws EC2Exception {
        ec2.attachVolume(vol.getVolumeId(), inst.getInstanceId(), "2");
    }
    public void attachVolume(ReservationDescription.Instance inst, VolumeInfo vol, int device) throws EC2Exception {
        ec2.attachVolume(vol.getVolumeId(), inst.getInstanceId(), String.valueOf(device));
    }
}
