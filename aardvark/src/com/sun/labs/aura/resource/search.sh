PREFIX=01
AURAHOME=/scratch2/stgreen/aura

nohup /net/lur/big/Projects/LabsUtil/jini/startReggieNB.sh &> ${AURAHOME}/reggie.out &

sleep 5

nohup java -DauraHome=${AURAHOME} \
      -jar dist/aardvark.jar \
      /com/sun/labs/aura/resource/dataStoreHeadConfig.xml \
      dataStoreHeadStarter &> dsh.out &

sleep 5

nohup java -DauraHome=${AURAHOME} -Dprefix=${PREFIX} \
      -jar dist/aardvark.jar \
      /com/sun/labs/aura/resource/partitionClusterConfig.xml \
      partitionClusterStarter &> pc.out &

sleep 5

nohup java -DauraHome=${AURAHOME} -Dprefix=${PREFIX} \
      -jar dist/aardvark.jar \
      /com/sun/labs/aura/resource/replicantConfig.xml \
      replicantStarter &> rep.out & 

