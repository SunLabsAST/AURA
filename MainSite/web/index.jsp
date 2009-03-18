<%-- 
     Document   : index
     Created on : Sep 2, 2008, 4:46:33 PM
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
                    TasteKeeper is the public face of the AURA Project
                    of <a href="http://research.sun.com">Sun Microsystems
                    Laboratories</a>.  The AURA project was started to
                    investigate new recommender technologies.  A recommender
                    system provides recommendations for items (books, music,
                    movies, etc.) based on a user's previous preferences (books
                    they've read, music they've listened to, etc.)
                </p>
            </div>
            <div class="sectionTitle">TasteKeeper's Recommendations</div>
            <div class="regularTxt">
                <p>
                    Current recommender systems rely on the wisdom of
                    the crowds using a technique
                    called <em>collaborative filtering</em>.  In a
                    collaborative filtering system, the similarity
                    between two items (e.g., books, songs, movies) is
                    determined by looking at what users have consumed
                    the items.  Items that have been consumed by many
                    similar users are considered to be similar.
                    Unfortunately, this approach has several drawbacks
                    that can ultimately lead
                    to <a href="http://blogs.sun.com/plamere/category/freakomendations">poor
                    recommendations</a>.
                </p>
                <p>
                    TasteKeeper takes a novel approach to
                    recommendation, avoiding many of the problems
                    inherent in standard recommender systems.  In
                    addition to representing an item by the users that
                    have consumed it, we also represent an item using
                    a <em>textual aura</em> that can be gathered from
                    social tags applied to the items, reviews of the
                    items, blog posts about the items, and even the
                    content of the items themselves.
                </p>

                <ul><li><a href="recommendation.jsp">Learn more about TasteKeeper's recommendations</a></li></ul>

           </div>
            <div class="sectionTitle">The AURA Data Store</div>
            <div class="regularTxt">
                <p>
                    The heart of the TasteKeeper system is the AURA
                    Data Store.  The Data Store was designed to be a
                    distributed, scalable, reliable data store that
                    could be used as a repository for item data and
                    metadata as well as user information and attention
                    data that could be used by recommenders for
                    various item types.  Our aim is to be able to
                    store information about millions of items and
                    potentially billions of attention data points.
                </p>

                <ul>
                    <li>
                        <a href="dataStore.jsp">Learn more about the AURA Project
                        Data Store</a>
                    </li>
                </ul>
            </div>
            <div class="sectionTitle">Try It Now</div>
            <div class="regularTxt">
                <p>
                    <a href="http://music.tastekeeper.com/"><img src="images/wmelogo.png"
                                                                 align="right"></a>
                    The <a href="http://music.tastekeeper.com/">Music
                    Explaura</a> is a musical artist recommendation application
                    built using TasteKeeper as its back end.  Users can find
                    artists they like, accentuate the aspects of those artists
                    they enjoy the most and get recommendations for other
                    artists.  Users can steer the recommendations by adding
                    terms that describe what they want and see explanations of
                    why particular artists were recommended.
                </p>
            </div>
            <div class="sectionTitle">Learn More</div>
            <div class="regularTxt">
                <p>
                    In the near future we will be releasing the AURA Project
                    in open source so that others can use and contribute to the project.
                </p>
                <p>
                    Some resources for learning more about The AURA Project:
                </p>
                <ul>
                    <li><a href="recommendation.jsp">TasteKeeper's
                    recommendation technology</a></li>
                    <li><a href="http://music.tastekeeper.com">The Music Explaura</a></li>
                    <li><a href="http://mediacast.sun.com/users/lamere/media/MusicExplauraTwoPager/details">A description of
                        the Music
			Explaura's transparent, steerable recommendations</a></li>
                    <li><a href="http://www.youtube.com/watch?v=wBgwnKV892I">A screencast of
                        the Music
                    Explaura in action</a></li>
                    <li><a href="http://forums.tastekeeper.com/">The
                        TasteKeeper discussion
                    forums</a></li>
                </ul>
            </div>
        </div>
    <%@include file="/WEB-INF/jspf/footer.jspf"%>
    </body>
</html>
