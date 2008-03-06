PREFIX=01
AURAHOME=/scratch2/stgreen/aura

nohup java -DauraHome=${AURAHOME} \
      -jar dist/aardvark.jar \
      /com/sun/labs/aura/resource/recommenderManagerConfig.xml \
      recommenderManagerStarter &> reccm.out &

sleep 10

nohup java -DauraHome=${AURAHOME} \
      -jar dist/aardvark.jar \
      /com/sun/labs/aura/aardvark/resource/aardvarkConfig.xml \
      aardvarkStarter &> aardvark.out &
