PREFIX=01
AURAHOME=/scratch2/stgreen/aura

nohup /net/lur/big/Projects/LabsUtil/jini/startReggieNB.sh &> ${AURAHOME}/reggie.out &

nohup java -DauraHome=${AURAHOME} \
      -jar dist/aardvark.jar \
      /com/sun/labs/aura/resource/dataStoreHeadConfig.xml \
      dataStoreHeadStarter &> dsh.out &

