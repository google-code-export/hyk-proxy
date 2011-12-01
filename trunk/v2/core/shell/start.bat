@echo off
set HYK_PROXY_HOME=%~dp0\..
@java -cp "%~dp0\..\lib\hyk-proxy-core.jar;%~dp0\..\conf" "-DHYK_PROXY_HOME=%HYK_PROXY_HOME%" org.hyk.proxy.core.launch.ApplicationLauncher cli