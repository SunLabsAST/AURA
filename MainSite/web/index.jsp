<%-- 
    Document   : index
    Created on : Sep 2, 2008, 4:46:33 PM
    Author     : ja151348
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" type="text/css" href="<c:url value="/style/main.css"/>">
        <title>TasteKeeper</title>
    </head>
    <body>
        <%@include file="/WEB-INF/jspf/header.jspf"%>
        <%@include file="/WEB-INF/jspf/sidebar.jspf"%>
        <div class="main">
        <div class="mainTitle"><img src="images/tklogo.png"></div>
        <span class="sectionTitle">What is TasteKeeper?</span><br/>
        <div class="regularTxt">
            <p>
            TasteKeeper is a scalable cross-domain open recommendation service.
            Client applications contribute taste data (tags, play counts, thumbs
            up, etc) and can request recommendations based on their users' tastes.
            The more clients contribute, the better the whole system gets.
            </p>
            <p>
            TasteKeeper uses the "aura" around items (reviews, descriptions,
            tags, etc) to generate recommendations, so it is capable of
            recommending appropriate items that popularity-based recommenders
            miss.
            </p>
        </div>
        <div class="sectionTitle">Why use TasteKeeper?</div>
        <div class="regularTxt">
            <p>
            Many web sites and applications can benefit from offering
            recommendation capabilities to their users, but not many have a
            large enough user base to generate the necessary data.  By working
            together through TasteKeeper, the whole can be greater than the
            sum of its parts.
            </p>
            <p>
            TasteKeeper provides a benefit to your users in that their taste
            data isn't locked into a closed system.  Users automatically bring
            their profile with them when they come to your site from any other
            site that uses TasteKeeper.  Users of the system have a right to
            their data and will always be allowed to retrieve all the data
            that TasteKeeper saves for them.
            </p>
        </div>
        <div class="sectionTitle">Try It Now</div>
        <div class="regularTxt">
            <p>
            <a href="http://music.tastekeeper.com/><img src="images/wmelogo.png" align=right></a>
            The <a href="http://music.tastekeeper.com/">Music Explaura</a>
            is an example application built using
            TasteKeeper as its back end.  It is a musical artist recommendation
            application.  Users can select artists they like, accentuate the
            aspects of those artists they enjoy the most and get
            recommendations for other artists.  Users can steer the recommendations
            by adding terms that describe what they want and see explanations
            of why particular artists were recommended.  The Music Explaura
            can also import your listening habits from last.fm.
            </p>
            <p>
            See how TasteKeeper can give custom, steerable, on-the-fly
            recommendations using the <a href="http://music.tastekeeper.com/">Music Explaura</a>.
            </p>
        </div>
        <div class="sectionTitle">Learn More</div>
        <div class="regularTxt">
            <p>
            Learn more about how to use TasteKeeper, and what it does.
            <ul>
                <li>TasteKeeper's <a href="">recommendation</a> technology</li>
                <li><a href="">Adding TasteKeeper</a> to your application</li>
                <li><a href="http://aura.kenai.com/">Project AURA</a>, the code for TasteKeeper</li>
                <li>The <a href="http://www.projectcaroline.net/">platform</a> that supports TasteKeeper</li>
                <li>Our <a href="http://music.tastekeeper.com/api">API</a> for music</li>
            </ul>
            </p>
        </div>
        </div>
    </body>
</html>
