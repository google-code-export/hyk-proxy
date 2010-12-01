@echo off
set HYK_PROXY_HOME=%~dp0\..
@java -cp "%HOME%\.hyk-proxy\.update\lib\hyk-proxy-launch.jar;%HOME%\.hyk-proxy\.update\etc;%~dp0\..\lib\hyk-proxy-framework.jar;%~dp0\..\etc" "-DHYK_PROXY_HOME=%HYK_PROXY_HOME%"  com.hyk.proxy.framework.launch.Launcher stopfr