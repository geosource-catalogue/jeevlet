set JEEVLET_LIB=..\target
set GEONETWORK_LIB=..\..\web\target

FOR %%f IN (%JEEVLET_LIB%\*.jar) DO CALL add2lib %%f
FOR %%f IN (%GEONETWORK_LIB%\WEB-INF\lib\*.jar) DO CALL add2lib %%f

SET CLASSPATH=%PROJECT_LIB%

java -classpath %CLASSPATH% jeevlet.StopperAuto

exit
