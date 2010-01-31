#!/bin/bash

HYK_PROXY_BIN=`dirname $0 | sed -e "s#^\\([^/]\\)#${PWD}/\\1#"` # sed makes absolute
HYK_PROXY_HOME=$HYK_PROXY_BIN/..
HYK_PROXY_LIB=$HYK_PROXY_HOME/lib
HYK_PROXY_CONFIG=$HYK_PROXY_HOME/etc

java  -cp "$HYK_PROXY_HOME/dist/hyk-proxy-client.jar:$HYK_PROXY_CONFIG" com.hyk.proxy.gae.client.netty.StartProxyLocalServer