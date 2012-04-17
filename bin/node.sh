#!/bin/bash

gnusername="$1"
gnpassword="$2"
gnnodeid="$3"
gnnodeprefix="geosource"
gnwebappname="geosource"
gninstalldir="/applications/geosource"
gndburl="$4"
gndbdriver="$5"
gndbdriver_default="org.postgresql.Driver"
gnpoolsize="$6"
gnpoolsize_default=2
gnminIdle=0
gnmaxIdle=$gnpoolsize

GEONETWORK_HOME=$7


# Set default values
if [ -z "$gndbdriver" ]
then
  gndbdriver=$gndbdriver_default
fi
if [ -z "$gnpoolsize" ]
then
  gnpoolsize=$gnpoolsize_default
fi
if [ -z "$GEONETWORK_HOME" ]
then
  GEONETWORK_HOME=../../web/geosource
fi

GEONETWORK_LIB=$GEONETWORK_HOME"/WEB-INF/lib"

GEONETWORK_SRC=$GEONETWORK_HOME/WEB-INF
GEONETWORK_TARGET=$GEONETWORK_HOME/WEB-INF-$gnnodeid

function showUsage 
{
  echo -e "\nThis script is used to create a new node configuration" 
  echo -e "\n  Default pool size: $gnpoolsize_default" 
  echo -e "  Default db driver: $gndbdriver_default"
  echo -e "  Default webapp path: $GEONETWORK_HOME"
  echo
  echo -e "Usage: ./`basename $0 $1` username password nodeid dburl"
  echo -e "       ./`basename $0 $1` username password nodeid dburl dbdriver"
  echo -e "       ./`basename $0 $1` username password nodeid dburl dbdriver dbpoolsize"
  echo -e "       ./`basename $0 $1` username password nodeid dburl dbdriver dbpoolsize webapppath"
  echo
  echo -e "Example:"
  echo -e "\t./`basename $0 $1` admin admin 42 jdbc:postgresql://localhost:5432/catdb"
  echo -e "\t./`basename $0 $1` admin admin 42 jdbc:postgresql://localhost:5432/catdb org.postgresql.Driver "
  echo -e "\t./`basename $0 $1` admin admin 42 jdbc:postgresql://localhost:5432/catdb org.postgresql.Driver 5"
  echo -e "\t./`basename $0 $1` admin gnos 42 jdbc:h2:/tmp/geonetwork42 org.h2.Driver 2"
  echo -e "\t./`basename $0 $1` admin admin 42 jdbc:postgresql://localhost:5432/catdb org.postgresql.Driver 2 ../../web/geonetwork"
  echo
}

echo $#

if [ "$1" = "-h" ] 
then
	showUsage
	exit
fi

if [ $# -lt 5 ]
then
  showUsage
  exit
fi
echo "Creating node  : $gnnodeid"
echo "      DB driver: $gndbdriver"
echo "      DB URL   : $gndburl"
echo "      DB user  : $gnusername"
echo "Creating directory ..."
mkdir $GEONETWORK_HOME/WEB-INF-$gnnodeid
mkdir $gninstalldir/data/$gnnodeid
mkdir $GEONETWORK_HOME/WEB-INF-$gnnodeid/lucene

echo "Copying default configuration file ..."
cp $GEONETWORK_SRC/config-gui.xml $GEONETWORK_TARGET/.
cp $GEONETWORK_SRC/config-lucene.xml $GEONETWORK_TARGET/.
cp $GEONETWORK_SRC/config-stats-params.xml $GEONETWORK_TARGET/.
cp $GEONETWORK_SRC/config-summary.xml $GEONETWORK_TARGET/.
cp $GEONETWORK_SRC/config-notifier.xml $GEONETWORK_TARGET/.
cp $GEONETWORK_SRC/geoserver-nodes.xml $GEONETWORK_TARGET/.
#ln -s $GEONETWORK_SRC/log4j.cfg $GEONETWORK_TARGET/log4j.cfg
cp $GEONETWORK_SRC/log4j.cfg $GEONETWORK_TARGET/.
cp $GEONETWORK_SRC/mime-types.properties $GEONETWORK_TARGET/.
cp $GEONETWORK_SRC/oasis-catalog.xml $GEONETWORK_TARGET/.
cp $GEONETWORK_SRC/schemaplugin-uri-catalog.xml $GEONETWORK_TARGET/.
cp $GEONETWORK_SRC/server.prop $GEONETWORK_TARGET/.
cp $GEONETWORK_SRC/user-profiles.xml $GEONETWORK_TARGET/.
cp $GEONETWORK_SRC/web.xml $GEONETWORK_TARGET/.

echo "Setting db connection ..."
java -classpath xalan-2.7.1.jar:serializer-2.7.1.jar org.apache.xalan.xslt.Process \
	-PARAM user $gnusername \
	-PARAM password $gnpassword \
	-PARAM idNode $gnnodeid \
	-PARAM gnnodeprefix $gnnodeprefix \
	-PARAM gninstalldir $gninstalldir \
	-PARAM gnwebappname $gnwebappname \
	-PARAM dbDriver $gndbdriver \
	-PARAM dbUrl $gndburl \
	-PARAM poolSize $gnpoolsize \
	-IN $GEONETWORK_SRC/config.xml -XSL update-config.xsl \
	-OUT $GEONETWORK_TARGET/config.xml 

echo "Done."
