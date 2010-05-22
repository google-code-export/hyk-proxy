@echo off
set HYK_PROXY_CLIENT_HOME=%~dp0\..
@java -cp "%~dp0\..\dist\hyk-proxy-client.jar;%~dp0\..\etc" "-DHYK_PROXY_CLIENT_HOME=%HYK_PROXY_CLIENT_HOME%" com.hyk.proxy.client.application.gae.admin.Admin %*