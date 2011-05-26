
# Add all libs to classpath
JEEVLET_LIB=../target
JEEVLET_DEP_LIB=$JEEVLET_LIB/lib
GEONETWORK_LIB=../../web/target
#GEONETWORK_LIB=/home/francois/Neogeo3/Workspace/Catalogue/GS-MN/web/target
for f in $JEEVLET_LIB/*.jar; do CLASSPATH=$CLASSPATH:$f; done
for f in $JEEVLET_DEP_LIB/*.jar; do CLASSPATH=$CLASSPATH:$f; done
for f in ../lib/*.jar; do CLASSPATH=$CLASSPATH:$f; done
for f in $GEONETWORK_LIB/*.jar; do CLASSPATH=$CLASSPATH:$f; done
for f in $GEONETWORK_LIB/geonetwork/WEB-INF/lib/*.jar; do CLASSPATH=$CLASSPATH:$f; done
echo $CLASSPATH

# FIXME : Remove Xalan from classpath because it may be used by Jeeves.TransformerFactoryFactory and cause XSL issues
rm $JEEVLET_DEP_LIB/xalan-*.jar

# Start the app
java -Xms128m -Xmx1024m -XX:MaxPermSize=128m -classpath $CLASSPATH jeevlet.StarterAuto `pwd` $*
