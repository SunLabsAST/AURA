
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Registration</title>
        <link rel="stylesheet" type="text/css" href="<c:url value="/style/aardvark.css"/>">
    </head>
    <body>
        <div align="center">
            <img src="<c:url value="/images/aardvark-still.gif" />" />
        </div>
        <div class="bigOrangeTxt">Welcome</div>
        <div class="explanation">
        Before you register with Aardvark, you need to have an OpenID that will
        act as your login for Aardvark.  OpenID is an open framework that allows
        you to log in with a site that you trust, then rely on that site to
        prove that you are who you say you are.  If you haven't already done
        so, please <a href="http://myopenid.com/">create</a> an OpenID for yourself.
        <p/>
        Now you are ready to register.  Please complete the registration form
        below and we'll ask you to authenticate your OpenID before creating your
        account.
        </div>
        <div class="bigOrangeTxt">Registration</div>
        <div class="explanation">
            <c:if test='${alreadyRegistered == "true"}'>
            <span class="smallGreenTxt">Sorry, that ID is already registered!</span>
            </c:if>
        <form method="post" action="<c:url value="/Register"/>">
            <table>
                <tr><td align="right">OpenID:</td>
                <td align="left"><input type="text" size="30" name="openid_url" maxlength="512" class="openidField" value="${openid_url}"/></td></tr>
                <tr><td align="right">Default Feed:</td>
                <td align="left"><input type="text" size="40" name="default_feed" maxlength="1024"/></td></tr>                
            </table>
            <input type="submit" value="Register"/>
        </form>
        </div>
    </body>
</html>
