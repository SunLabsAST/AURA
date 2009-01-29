#!/bin/bash
export BUCKET=aura-amis
export EC2_URL=https://ec2.amazonaws.com 
export EC2_PRIVATE_KEY=/mnt/keys/pk-6FBPDOAQ3VL2JVFOZQZELR4CUMLAVV47.pem 
export EC2_CERT=/mnt/keys/cert-6FBPDOAQ3VL2JVFOZQZELR4CUMLAVV47.pem 
export EC2_KEYID=07ADZZ039NDF6A3MN7G2
export EC2_KEY=7XG7cp3TOFKSxQPbW7QDVNxkeDNi0wI85Q9BXUm7
export DIRECTORY=/mnt
export IMAGE=$1

cd $DIRECTORY

mkdir parts
mkdir keys

rebundle.sh -v $IMAGE

ec2-bundle-image -c $EC2_CERT -k $EC2_PRIVATE_KEY   \
   --kernel aki-6552b60c --ramdisk ari-6452b60d \
   --block-device-mapping "root=rpool/52@0,ami=0,ephemeral0=1" \
   --user 392042932095 --arch i386 \
   -i $DIRECTORY/$IMAGE -d $DIRECTORY/parts

ec2-upload-bundle -b $BUCKET  -m $DIRECTORY/parts/$IMAGE.manifest.xml \
    --url http://s3.amazonaws.com \
    --retry -a $EC2_KEYID -s $EC2_KEY 

ec2reg -C $EC2_CERT  \
-K $EC2_PRIVATE_KEY $BUCKET/$IMAGE.manifest.xml
