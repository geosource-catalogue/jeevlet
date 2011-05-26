# Add all libs to classpath
set JEEVLET_LIB=..\target
set JEEVLET_DEP_LIB=%JEEVLET_LIB%\lib
set GEONETWORK_LIB=..\..\web\target

FOR %%f IN (%JEEVLET_LIB%\*.jar) DO CALL add2lib %%f
FOR %%f IN (%JEEVLET_DEP_LIB%\*.jar) DO CALL add2lib %%f
FOR %%f IN (%GEONETWORK_LIB%\*.jar) DO CALL add2lib %%f
FOR %%f IN (%GEONETWORK_LIB%\geonetwork\WEB-INF\lib*.jar) DO CALL add2lib %%f

SET CLASSPATH=%PROJECT_LIB%

# FIXME : Remove Xalan from classpath because it may be used by Jeeves.TransformerFactoryFactory and cause XSL issues
del $JEEVLET_DEP_LIB\xalan-*.jar

# Start the app
java -Xms128m -Xmx1024m -XX:MaxPermSize=128m -classpath %CLASSPATH% jeevlet.StarterAuto %1%
