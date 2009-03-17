<%-- 
     Document   : index
     Created on : Sep 2, 2008, 4:46:33 PM
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
	<p>
	  Current recommender systems rely on the wisdom of the
	  crowds using a technique called <em>collaborative
	    filtering</em>.  In a collaborative filtering system,
	  the similarity between two items (e.g., books, songs,
	  movies) is determined by looking at what users have
	  consumed the items.  Items that have been consumed by
	  many similar users are considered to be similar.
	</p>
	<p>
	  Unfortunately, this approach has several drawbacks that
	  can ultimately lead
	  to <a href="http://blogs.sun.com/plamere/category/freakomendations">poor
	    recommendations</a>.  In particular, collaborative
	  filtering systems suffer from the <em>cold start
	    problem</em>.  A new item that has not been consumed
	  cannot be recommended.  This is especially a problem for a
	  new recommender.  As people begin to discover more and
	  more things via recommendation, feedback loops can develop
	  where only recommended items get popular and only popular
	  items get recommended.
	</p>
	<p>
	  The AURA Project takes a novel approach to recommendation,
	  avoiding many of the problems inherent in standard
	  recommender systems.  In addition to representing an item by
	  the users that have consumed it, we also represent an item
	  using a <em>textual aura</em> that can be gathered from
	  social tags applied to the items, reviews of the items, blog
	  posts about the items, and even the content of the items
	  themselves.
	</p>
	<p>
	  Given this textual aura, we can compute the similarity
	  between two items based on the similarity of the textual
	  auras of the two items.  This approach avoids the cold start
	  problem and provides high quality recommendations.
	  Recommendations generated this way are <em>transparent</em>:
	  we can explain why an item was recommended using the words
	  from the textual aura.  We can also use the words from the
	  textual aura to <em>steer</em> the recommendations the
	  system is making by increasing the impact of particular
	  words and decreasing or removing the impact of other words.
	</p>
      </div>
     <div class="sectionTitle">The Aura Data Store</div>
      <div class="regularTxt">
	<p>
	  The heart of the Aura system is the Aura Data Store.
	</p>
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
	<p>
	  The Music Explaura is running on a 16 node data store
	  that is capable of supporting 14,000 concurrent users
	  doing typical recommendation tasks.
	</p>
      </div>
    </div>
    <div class="sectionTitle">Learn More</div>
    <div class="regularTxt">
      <p>
	In the near future we will be opening sourcing Project
	AURA so that others can use and contribute to the project.
      </p>
      <p>
	Some resources for learning more about The AURA Project:
      </p>
      <ul>
        <li>TasteKeeper's <a href="recommendation.jsp">recommendation</a>
          technology</li>
	<li>A description of
	  the <a href="http://mediacast.sun.com/users/lamere/media/MusicExplauraTwoPager/details">Music
	    Explaura's transparent, steerable recommendations</a></li>
	<li>A screencast of the <a href="http://www.youtube.com/watch?v=wBgwnKV892I">Music Explaura in action</a></li>
        <li>The TasteKeeper <a href="http://forums.tastekeeper.com/">discussion forums</a></li>
        <li><a href="http://aura.kenai.com/">The AURA Project</a>, the code for TasteKeeper</li>
      </ul>
    </div>
</div>
</body>
</html>
