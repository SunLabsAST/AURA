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
<jsp:useBean id="statsBean" class="com.sun.labs.aura.aardvark.web.bean.StatsBean" scope="request"/>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" type="text/css" href="<c:url value="/style/aardvark.css"/>">
        <link rel="Shortcut Icon" href="<c:url value="/favicon.ico"/>">
        <title>Home</title>
    </head>
    <body>
        <%@include file="/WEB-INF/jspf/header.jspf"%>
        <div class="main">
        <div align="center">
            <img src="<c:url value="/images/aardvark-still.gif" />" />
        </div>
        <div class="bigOrangeTxt">Welcome <%= userBean.getNickname() %></div>
        Your starred item feed URL is: <a href="<%= userBean.getDefaultFeedURL() %>"><%= userBean.getDefaultFeedURL() %></a><br>
        Your recommended feed URL is: <a href="<c:url value="${userBean.recommendedFeedURL}"/>"><c:url value="${userBean.recommendedFeedURL}"/></a><br>
        User id is <%= userBean.getID() %><br>
        Full name is <%= userBean.getFullname() %><br>
        </div>
    </body>
</html>
