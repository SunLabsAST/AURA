<%-- 
    Document   : index
    Created on : Dec 8, 2008, 1:12:16 PM
    Author     : ja151348
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Test Login Page</title>
    </head>
    <body>
        <h1>Test login service of tastekeeper.</h1>
        <form action='<c:url value="/wslogin"/>'>
            Enter your Open ID:&nbsp;&nbsp;
            <input type="text" size="30" name="openid" maxlength="512"/>
            <input type="hidden" name="return" value="<c:url value="/index.jsp"/>"/>
            <input type="submit" value="Test"/>
        </form>
            <c:if test='${not empty param.tksession}'>
            <br/>
            <h2>
                TK Session = <c:out value="${param.tksession}"/>
                <br/>
                For User ID <c:out value='${sessionScope["openid"]}'/>
            </h2>
            </c:if>
            <c:if test='${not empty param.error}'>
                Error: <c:out value="${param.error}"/>
            </c:if>
    </body>
</html>
