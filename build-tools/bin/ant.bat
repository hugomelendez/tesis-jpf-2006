#!/bin/bash
#
# run ant with libs from the jpf-build-tools module
#

JPF_TOOLS_HOME=`dirname $0`/..

CP=""


# handle the icky differences of *nix-ish Java installations
_cygwin=false;
_darwin=false;
case "`uname`" in
  CYGWIN*) _cygwin=true
           if [ -z "$JAVA_HOME" ]; then
             echo "JAVA_HOME not set, cannot locate the compiler"
             exit 1
           fi
           J_HOME=`cygpath --path --unix "$JAVA_HOME"`
           JAVAC_JAR="$J_HOME/lib/tools.jar"
           if [ ! -f "$JAVAC_JAR" ]; then
             echo "no bytecode compiler found: $JAVAC_JAR"
             exit 1
           fi
           CP=$JAVAC_JAR

           ;;
           
  Darwin*) _darwin=true
           # nothing to do, sun.tools.javac.Main should be in classes.jar
           ;;
           
  *)       if [ -z "$JAVA_HOME" ]; then
             echo "JAVA_HOME not set, cannot locate the compiler"
             exit 1
           fi
           
           JAVAC_JAR="$JAVA_HOME/lib/tools.jar"
           if [ ! -f "$JAVAC_JAR" ]; then
             echo "no bytecode compiler found: $JAVAC_JAR"
             exit 1
           fi
           CP=$JAVAC_JAR
           ;;
esac


# add all jars and zips we find in the build-tools/lib dir
CP=$CP:`find $JPF_TOOLS_HOME/lib \( -name "*.jar" -or -name "*.zip" \) -exec echo -n {}":" \;`

if $_cygwin; then
  CP=`cygpath --path --windows "$CP"`
  JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
fi

java -classpath "$CP" org.apache.tools.ant.Main $@
