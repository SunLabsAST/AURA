/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.grid.ec2;

import com.xerox.amazonws.ec2.KeyPairInfo;
import com.xerox.amazonws.ec2.LaunchConfiguration;
import com.xerox.amazonws.ec2.ReservationDescription;

/**
 *
 */
public class LaunchAuraInstances {

    public static void main(String[] args) throws Exception {
        if(args.length < 2) {
            System.err.println(String.format("Usage: LaunchInstance <image id> <keypair name>"));
            return;
        }
        String imageID = args[0];
        String keyPairName = args[1];

        EC2Grid grid = new EC2Grid();
        KeyPairInfo kpi = grid.getKeyPairInfo(keyPairName);

        //
        // Retrieve or create a key pair.

        LaunchConfiguration lc = new LaunchConfiguration(imageID);
        lc.setKeyName(keyPairName);

        //
        // Launch!
//        ReservationDescription rd = ec2.runInstances(lc);
    }
}
