<%-- 
    Document   : index
    Created on : Mar 30, 2009, 1:39:23 PM
    Author     : ja151348
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        This is the Caroline web server visualization agent.  It is currently
        reporting <c:out value="${activeSessions}"/> active sessions.<br/>
        <c:if test="${isNewSession}">
            This is a new session.
        </c:if>
    </body>
</html>
