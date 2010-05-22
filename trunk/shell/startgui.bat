@echo off
set HYK_PROXY_CLIENT_HOME=%~dp0\..
@start javaw -cp "%~dp0\..\dist\hyk-proxy-client.jar;%~dp0\..\etc" "-DHYK_PROXY_CLIENT_HOME=%HYK_PROXY_CLIENT_HOME%" com.hyk.proxy.client.launch.gui.GUILauncher %*