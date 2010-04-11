<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!-- The HTML 4.01 Transitional DOCTYPE declaration-->
<!-- above set at the top of the file will set     -->
<!-- the browser's rendering engine into           -->
<!-- "Quirks Mode". Replacing this declaration     -->
<!-- with a "Standards Mode" doctype is supported, -->
<!-- but may lead to some differences in layout.   -->

<html>
  <%@ page language="java" import="java.sql.*" %>
   <jsp:useBean id="version" scope="page" class="com.hyk.proxy.gae.common.Version" />
  <head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <title>hyk-proxy V<%out.print(version.value);%></title>
  </head>

  <body>
    <table width="800" border="0" align="center">
            <tr><td align="center">
                <b><h1>hyk-proxy V<%out.print(version.value);%> server is running!</h1></b>
            </td></tr>
            <tr><td align="center">
                <a href="/admin.jsp">admin</a>
            </td></tr>
    </table>
    
  </body>
</html>
