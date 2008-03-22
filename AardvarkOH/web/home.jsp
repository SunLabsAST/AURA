<%-- 
    Document   : home
    Created on : Mar 17, 2008, 1:38:43 PM
    Author     : ja151348
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<jsp:useBean id="userBean" class="com.sun.labs.aura.aardvark.web.bean.UserBean" scope="request"/>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <h2>Welcome <%= userBean.getRealName() %></h2>
        <h2>There are <%= userBean.getNumFeeds() %> feeds</h2>
    </body>
</html>
