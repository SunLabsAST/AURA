<%-- 
    Document   : viewAttention
    Created on : Apr 2, 2008, 5:05:30 PM
    Author     : ja151348
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<jsp:useBean id="userBean" class="com.sun.labs.aura.aardvark.web.bean.UserBean" scope="session"/>
<jsp:useBean id="statsBean" class="com.sun.labs.aura.aardvark.web.bean.StatsBean" scope="request"/>
<jsp:useBean id="attnBean" type="com.sun.labs.aura.aardvark.web.bean.AttentionBean[]" scope="request"/>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" type="text/css" href="<c:url value="/style/aardvark.css"/>">
        <link rel="Shortcut Icon" href="<c:url value="/favicon.ico"/>">
        <title>View Attentions</title>
    </head>
    <body>
        <%@include file="/WEB-INF/jspf/header.jspf"%>
        <div class="main">
            <div align="right" style="padding: 5px">
                <a href="<c:url value="/Home"/>" title="Home">Home</a>
                &nbsp;&nbsp;
                <a href="<c:url value="/Logout"/>" title="Logout">Logout</a>
            </div>
        <div align="center">
            <img src="<c:url value="/images/aardvark-still.gif" />" />
        </div>
        <div class="bigOrangeTxt">Collected Attention for <%= userBean.getNickname() %></div>
        <div class="regularTxt">
        We have collected ${fn:length(attnBean)} individual taste points for you.
        <!-- Print out the table of all collected attention -->
        <div class="table">
        <c:forEach var="attn" items="${attnBean}" varStatus="loop">
            <div class="${((loop.index % 2) == 0 ? "row" : "shadedRow") }">
            <div class="attnTypeCol"><c:out value="${attn.type}"/></div>
            <div class="attnTargCol"><a href="<c:out value="${attn.targetKey}"/>"><c:out value="${attn.targetKeyName}"/></a></div>
            <div class="attnTimeCol"><c:out value="${attn.time}"/></div>
            </div> <!-- row -->
        </c:forEach>
        </div> <!-- table -->
        </div> <!-- main -->
        </body>
</html>
