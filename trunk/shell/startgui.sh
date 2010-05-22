#!/bin/bash

#this part is copied from ANt's script
# OS specific support.  $var _must_ be set to either true or false.
cygwin=false;
case "`uname`" in
  CYGWIN*) cygwin=true ;;
esac

HYK_PROXY_BIN=`dirname $0 | sed -e "s#^\\([^/]\\)#${PWD}/\\1#"` # sed makes absolute
HYK_PROXY_HOME=$HYK_PROXY_BIN/..
HYK_PROXY_LIB=$HYK_PROXY_HOME/lib
HYK_PROXY_CONFIG=$HYK_PROXY_HOME/etc
CLASSPATH="$HYK_PROXY_HOME/dist/hyk-proxy-client.jar:$HYK_PROXY_CONFIG"
if $cygwin; then
  if [ "$OS" = "Windows_NT" ] && cygpath -m .>/dev/null 2>/dev/null ; then
    format=mixed
  else
    format=windows
  fi
  CLASSPATH=`cygpath --path --$format "$CLASSPATH"`
  HYK_PROXY_HOME=`cygpath --path --$format "$HYK_PROXY_HOME"`
fi

java  -cp "$CLASSPATH" -DHYK_PROXY_CLIENT_HOME="$HYK_PROXY_HOME" com.hyk.proxy.client.launch.gui.GUILauncher