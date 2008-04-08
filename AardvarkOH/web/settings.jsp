<%-- 
    Document   : settings
    Created on : Apr 3, 2008, 11:00:13 PM
    Author     : jalex
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<jsp:useBean id="userBean" class="com.sun.labs.aura.aardvark.web.bean.UserBean" scope="session"/>
<jsp:useBean id="statsBean" class="com.sun.labs.aura.aardvark.web.bean.StatsBean" scope="request"/>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" type="text/css" href="<c:url value="/style/aardvark.css"/>">
        <link rel="Shortcut Icon" href="<c:url value="/favicon.ico"/>">
        <title>Aardvark Settings</title>
        
        <script type="text/javascript">
function removeFeed(form, index) {
    form.toRemove.value = index;
    form.submit();
}
        </script>
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
        <div class="bigOrangeTxt">
            Settings
        </div>
        <div class="regularTxt">
        <form method="post" action="<c:url value="/Settings"/>">
            The following information is used to customize how we refer to you:
        <div class="promptTxt">
        <table>
            <tr>
            <td align="right">Your nickname:</td>
            <td><input type="text" name="nickname" maxlength="50" size="30" value="${userBean.nickname}"/></td>
            </tr><tr>
            <td align="right">Your full name:</td>
            <td><input type="text" name="fullname" maxlength="50" size="30" value="${userBean.fullname}"/></td>
            </tr><tr>
            <td align="right">Your Email Address:</td>
            <td><input type="text" name="email" maxlength="50" size="30" value="${userBean.emailAddress}"/></td>
            </tr>
        </table>
        </div>
            The following settings allow you to customize the behavior of your
            feeds:
        <table>
            <tr>
            <td align="right"><span class="promptTxt">Flood feed size:</span></td>
            <td><input type="text" name="floodSize" maxLength=3 size="2"></td>    
            </tr>
        </table>
        <input type="hidden" value="UpdateSettings" name="op"/>
        <input type="submit" value="Update Settings"/>
        </form>
        </div>
        <div class="bigOrangeTxt">
            Feed Management
        </div>
        <div class="regularTxt">
        These are the feeds that we're currently monitoring that describe your
        taste.  You may remove any of the feeds in order to prevent them from
        being used in the future.  The text box at the bottom allows you to
        add another feed.  The entries in these feeds provide the basis for
        your recommendations.<p/>
        <form method="post" action="<c:url value="/Settings"/>">
        <input type="hidden" value="RemoveFeed" name="op"/>
        <input type="hidden" name="toRemove" value="" />
        <c:forEach var="feed" items="${userBean.basisFeeds}" varStatus="iter">
            <c:if test="${iter.first != iter.last}">
            <input type="button" value="Remove" onClick="removeFeed(this.form, ${iter.index})"/>
            </c:if>
            <a href="${feed}">${feed}</a><br>
        </c:forEach>
        </form>
        <form method="post" action="<c:url value="/Settings"/>">
        <span class="promptTxt">Add a new feed:</span>
        <input type="text" name="newFeed" size="60" value="${prevNewFeed}"/>
        <input type="hidden" value="AddFeed" name="op">
        <input type="submit" value="Add">
        </form>
        </div>
    </body>
</html>
