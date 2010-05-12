<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <link type="text/css" rel="stylesheet" href="PhotoViewer.css">
    <title>Photo Viewer</title>
    <script type="text/javascript" language="javascript" src="photoviewer/photoviewer.nocache.js"></script>
  </head>

  <body>
    <%
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();
    if (user != null) {
%>
	Hello, <%= user.getNickname() %> - <a href="<%= userService.createLogoutURL(request.getRequestURI()) %>">sign out</a>
	<input type="hidden" id="auth" value="true"></input>
<%
	} else {
%>
	<input type="hidden" id="auth" value="false"></input>
	<p>Please <a href="<%= userService.createLoginURL(request.getRequestURI()) %>">sign in</a> to enable links to larger images.</p>
<%
	}
%>
    <iframe src="javascript:''" id="__gwt_historyFrame" tabIndex='-1' style="position:absolute;width:0;height:0;border:0"></iframe>
    
    <noscript>
      <div style="width: 22em; position: absolute; left: 50%; margin-left: -11em; color: red; background-color: white; border: 1px solid red; padding: 4px; font-family: sans-serif">
        Your web browser must have JavaScript enabled
        in order for this application to display correctly.
      </div>
    </noscript>
    
    <div id="container">
      <div id="content">
	    <div id="input"></div>
	    <div id="output"></div>
	  </div>
	</div>
  </body>
</html>
