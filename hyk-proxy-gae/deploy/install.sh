#!/bin/bash

#this part is copied from ANt's script
# OS specific support.  $var _must_ be set to either true or false.
cygwin=false;
case "`uname`" in
  CYGWIN*) cygwin=true ;;
esac

BIN=`dirname $0 | sed -e "s#^\\([^/]\\)#${PWD}/\\1#"` # sed makes absolute
JAR=$BIN/AppCfgWrapper/AppCfgWrapper.jar
APP_LOCATION=BIN/./war

if $cygwin; then
  if [ "$OS" = "Windows_NT" ] && cygpath -m .>/dev/null 2>/dev/null ; then
    format=mixed
  else
    format=windows
  fi
  JAR=`cygpath --path --$format "$JAR"`
  APP_LOCATION=`cygpath --path --$format "$APP_LOCATION"`
fi

java "-Dappengine.app.location=%APP_LOCATION%" -jar $JAR