package com.sun.labs.aura.grid.ec2;

import com.xerox.amazonws.ec2.ImageDescription;
import com.xerox.amazonws.ec2.Jec2;
import java.util.Properties;

/**
 * Gets the info for a given set of images.
 */
public class ImageInfo {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

        if(args.length == 0) {
            System.err.println(String.format("Usage: ImageInfo <image id>+"));
            return;
        }
        Properties props = new Properties();
        props.load(Ec2Sample.class.getResourceAsStream("aws.properties"));
        Jec2 ec2 = new Jec2(props.getProperty("aws.accessId"),
                props.getProperty("aws.secretKey"));

        for(ImageDescription desc : ec2.describeImages(args)) {
            System.out.println(String.format("%s %s %s", desc.getImageId(), desc.getImageLocation(), desc.getProductCodes()));
        }
    }
}
