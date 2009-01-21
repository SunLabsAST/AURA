/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.grid.ec2;

import com.xerox.amazonws.ec2.EC2Exception;
import com.xerox.amazonws.ec2.Jec2;
import com.xerox.amazonws.ec2.KeyPairInfo;
import com.xerox.amazonws.ec2.LaunchConfiguration;
import com.xerox.amazonws.ec2.ReservationDescription;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author stgreen
 */
public class LaunchInstance {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        if(args.length < 2) {
            System.err.println(String.format("Usage: LaunchInstance <image id> <keypair name>"));
            return;
        }
        String imageID = args[0];
        String keyPairName = args[1];

        //
        // Connectez-vous?
        Properties props = new Properties();
        props.load(Ec2Sample.class.getResourceAsStream("aws.properties"));
        Jec2 ec2 = new Jec2(props.getProperty("aws.accessId"),
                props.getProperty("aws.secretKey"));

        //
        // Retrieve or create a key pair.
        KeyPairInfo kpi = null;
        try {
            List<KeyPairInfo> kpis = ec2.describeKeyPairs(new String[]{
                        keyPairName});
            kpi = kpis.get(0);
        } catch(EC2Exception ecex) {
            try {
                kpi = ec2.createKeyPair(keyPairName);
                //
                // Write the key pair to a file in ~/.ec2 so that we can find it later.
                BufferedWriter w = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(System.getProperty("user.home") +
                        File.separatorChar +
                        ".ec2" +
                        File.separatorChar + "id_rsa-" + keyPairName)));
                w.append(kpi.getKeyMaterial());
                w.close();
            } catch(EC2Exception ecex2) {
                System.err.println(String.format("Unable to get key pair information describe error " +
                        ecex + " create error " + ecex2));
                return;
            }
        }

        LaunchConfiguration lc = new LaunchConfiguration(imageID);
        lc.setKeyName(keyPairName);

        //
        // Launch!
        ReservationDescription rd = ec2.runInstances(lc);
    }

}
