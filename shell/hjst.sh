#!/bin/bash

#this part is copied from ANt's script
# OS specific support.  $var _must_ be set to either true or false.
cygwin=false;
case "`uname`" in
  CYGWIN*) cygwin=true ;;
esac

HYK_JST_BIN=`dirname $0 | sed -e "s#^\\([^/]\\)#${PWD}/\\1#"` # sed makes absolute
HYK_JST_HOME=$HYK_JST_BIN/..
HYK_JST_LIB=$HYK_JST_HOME/lib
HYK_JST_CONFIG=$HYK_JST_HOME/etc
CLASSPATH="$HYK_JST_HOME/dist/hyk-jsipunit.jar:$HYK_JST_CONFIG"
if $cygwin; then
  if [ "$OS" = "Windows_NT" ] && cygpath -m .>/dev/null 2>/dev/null ; then
    format=mixed
  else
    format=windows
  fi
  CLASSPATH=`cygpath --path --$format "$CLASSPATH"`
fi

java  -cp "$CLASSPATH" org.hyk.sip.test.launcher.Launcher $*