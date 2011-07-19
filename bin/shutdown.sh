JEEVLET_LIB=../target
JEEVLET_DEP_LIB=$JEEVLET_LIB/lib

for f in $JEEVLET_LIB/*.jar; do CLASSPATH=$CLASSPATH:$f; done
for f in $JEEVLET_DEP_LIB/*.jar; do CLASSPATH=$CLASSPATH:$f; done
for f in ../lib/*.jar; do CLASSPATH=$CLASSPATH:$f; done
echo $CLASSPATH


java -classpath $CLASSPATH jeevlet.StopperAuto

