@echo on

REM genpeerdisp - script to generate NativePeer dispatcher class

set JPF_HOME=%~dp0..\..

REM this is for GenPeerDispatcher itself (and the JPF infrastructure it might use)
set CP=%JPF_HOME%\build
set CP=%CP%;%JPF_HOME%\lib\jpf.jar
set CP=%CP%;%JPF_HOME%\lib\bcel.jar

REM this is where we keep our peer classes (the potential targets)
set CP=%CP%;%JPF_HOME%\build\env\jvm

java -classpath "%CP%;%CLASSPATH%" gov.nasa.arc.ase.jpf.tools.GenPeerDispatcher %1