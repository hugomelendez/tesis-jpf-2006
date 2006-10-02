@echo off

REM Set the LTL2Buchi_HOME directory
set LTL2Buchi_HOME=%~dp0..

REM where to find javac
set CP=%JAVA_HOME%\lib\tools.jar

REM this is the common ant stuff

REM If we have class files (e.g., with a source distribution), we probably
REM want to use those first
set CP=%CP%;%LTL2Buchi_HOME%\build\classes
set CP=%CP%;%LTL2Buchi_HOME%\build\test

REM Otherwise, we look for the jar (binary distributions)
set CP=%CP%;%LTL2Buchi_HOME%\lib\ltl2buchi.jar

java -classpath "%CP%" gov.nasa.ltl.trans.LTL2Buchi %1 %2 %3 %4 %5 %6 %7 %8 %9
