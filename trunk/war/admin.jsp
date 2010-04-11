<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.hyk.proxy.gae.server.core.service.AccountServiceImpl" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>

<html>
  <%@ page language="java" import="java.sql.*" %>
   <jsp:useBean id="version" scope="page" class="com.hyk.proxy.gae.common.Version" />
  <head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <title>hyk-proxy V<%out.print(version.value);%> admin</title>
  </head>
  <body>
    <table width="800" border="0" align="center">
            <tr><td align="center">
                <b><h1>root password:<%out.print(AccountServiceImpl.getRootUser().getPasswd());%></h1></b>
            </td></tr>
             <tr><td align="center">
                <%
                   UserService userService = UserServiceFactory.getUserService();
                   User user = userService.getCurrentUser();
                %>
                <a href="<%= userService.createLogoutURL("/") %>">sign out</a>
            </td></tr>
    </table>
  </body>
</html>
