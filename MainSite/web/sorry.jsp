<%-- 
    Document   : sorry
    Created on : Apr 6, 2009, 12:32:24 PM
    Author     : ja151348
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <link rel="stylesheet" type="text/css" href="<c:url value="/style/main.css"/>">
    <link rel="icon" type="image/png" href="images/tkfavicon.png"/>
    <title>Sorry</title>
  </head>
  <body>
    <%@include file="/WEB-INF/jspf/header.jspf"%>
    <%@include file="/WEB-INF/jspf/sidebar.jspf"%>
    <div class="main">
      <div class="mainTitle"><img src="images/tklogo.png"></div>
      <div class="sectionTitle">Sorry</div>
      <div class="regularTxt">
        Sorry, we seem to have had an error in our web servers.  Please try
        us again in a little while.
      </div>
    </div>
  </body>
</html>