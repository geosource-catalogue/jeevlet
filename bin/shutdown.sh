JEEVLET_LIB=../target
GEONETWORK_LIB=../../web/target

for jarFile in $JEEVLET_LIB/*.jar; do CLASSPATH="$CLASSPATH:$jarFile"; done
for jarFile in $GEONETWORK_LIB/geonetwork/WEB-INF/lib/*.jar; do CLASSPATH="$CLASSPATH:$jarFile"; done

java -classpath $CLASSPATH jeevlet.StopperAuto 
