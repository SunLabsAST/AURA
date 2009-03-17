<%-- 
    Document   : recommendation
    Created on : Nov 26, 2008, 11:29:12 AM
    Author     : ja151348
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<jsp:useBean id="statBean" class="com.sun.labs.aura.website.StatBean" scope="request"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" type="text/css" href="<c:url value="/style/main.css"/>">
        <title>TasteKeeper Recommendations</title>
    </head>
    <body>
        <%@include file="/WEB-INF/jspf/header.jspf"%>
        <%@include file="/WEB-INF/jspf/sidebar.jspf"%>
        <div class="main">
        <div class="mainTitle"><img src="images/tklogo.png"></div>
        <span class="sectionTitle">What makes a good recommendation?</span><br/>
        <div class="regularTxt">
            <p> 
	    There are three qualities that we like to think about
	    when generating recommendations.
	  </p>

	  <dl>
	    <dt>Novelty</dt><dd>Recommendations should be for items
	    that the user hasn't seen before.  Although we're trying
	    to find new things that people might like, we need to
	    be careful that users don't lose trust in the recommender
	    by never offering them anything that they recognize.</dd>
	    <dt>Serendipity</dt><dd>Users should be able to discover
	    new items in a serendipitous way.  For example, when a
	    user is looking for information about a particular item,
	    the recommendations should provide a way for them to
	    discover items that they might like.</dd>
	    <dt>Transparency</dt><dd>Users should be be able to 
	    understand why an item was recommended to them.</dd>
	  </dl>

       </div>
        <span class="sectionTitle">TasteKeeper's Recommendations</span><br/>
        <div class="regularTxt">
            <p>
            </p>
            <p>
            </p>
        </div>
        </div>
    </body>
</html>
