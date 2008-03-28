
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<jsp:useBean id="statsBean" class="com.sun.labs.aura.aardvark.web.bean.StatsBean" scope="request"/>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" type="text/css" href="<c:url value="/style/aardvark.css"/>">
        <link rel="Shortcut Icon" href="<c:url value="/favicon.ico"/>">
        <title>Feed the Aardvark</title>
    </head>
    <body>
        <%@include file="/WEB-INF/jspf/header.jspf"%>
        <div class="main">
            <div class="bigOrangeTxt">
                Feed Aardvark
            </div>
            <div class="regularTxt">
            <form method="get" action="<c:url value="/AddFeed"/>">
            Use the following field to add a feed for crawling to Aardvark:<br>
            <input type="text" size="60" name="feed"/>
            <input type="submit" value="Add" name="op"/><p>
            </form>
            <form method="get" action="<c:url value="/AddFeed"/>">
            To upload an OPML file of feeds, use the following uploader:<br>
            <input type="file" accept="text/*" name="opml"/>
            <input type="submit" value="Upload" name="op"/>
            </form>
            <c:if test='${not empty msg}'>
                <div class="statusMsg">
                <span class="smallGreenTxt"><c:out value="${msg}"/></span>
                </div>
            </c:if>
            </div>
        </div>
    </body>
</html>
