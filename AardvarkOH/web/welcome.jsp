<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">


<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Aardvark: An Open Recommender</title>
        <link rel="stylesheet" type="text/css" href="<c:url value="/style/aardvark.css"/>">
    </head>
    <body>
        <f:view>
            <div align="center">
                <img src="<c:url value="/images/aardvark-welcome.gif"/>">
            </div>
            <div align="center">
                Please log in with your <a href="http://www.openid.net">OpenID</a>:<br/>
                <form method="post" action="<c:url value="/Login"/>">
                    <input type="text" size="30" name="openid_url" maxlength="512" class="openidField"/>
                    <input type="submit" value="Login"/>
                </form>
                <span class="smallText">Don't have an ID?
                <a href="http://openid.sun.com/">Create</a> one, then
                <a href="<c:url value="/Register" />">register</a> yourself!</span>
            </div>
        </f:view>
    </body>
</html>
