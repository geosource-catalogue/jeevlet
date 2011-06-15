JEEVLET_LIB=../target
JEEVLET_DEP_LIB=$JEEVLET_LIB/lib

if [ -z "$GEONETWORK_HOME" ]
then
  GEONETWORK_HOME=../../web/geosource
fi
echo "Web app: $GEONETWORK_HOME"

# FIXME : Remove Xalan from classpath because it may be used by Jeeves.TransformerFactoryFactory and cause XSL issues
rm $JEEVLET_DEP_LIB/xalan-*.jar
rm $GEONETWORK_HOME/WEB-INF/lib/xalan-*.jar


# Add all libs to classpath
for f in $JEEVLET_LIB/*.jar; do CLASSPATH=$CLASSPATH:$f; done
for f in $JEEVLET_DEP_LIB/*.jar; do CLASSPATH=$CLASSPATH:$f; done
for f in ../lib/*.jar; do CLASSPATH=$CLASSPATH:$f; done
for f in $GEONETWORK_HOME/*.jar; do CLASSPATH=$CLASSPATH:$f; done
for f in $GEONETWORK_HOME/WEB-INF/lib/*.jar; do CLASSPATH=$CLASSPATH:$f; done
CLASSPATH=$CLASSPATH:$GEONETWORK_HOME/WEB-INF/classes
echo $CLASSPATH


# Start the app
java -Xms128m -Xmx1024m -XX:MaxPermSize=128m -classpath $CLASSPATH jeevlet.StarterAuto `pwd` $*
