<%-- 
     Document   : dataStore.jsp
  --%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
	  "http://www.w3.org/TR/html4/loose.dtd">
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <link rel="stylesheet" type="text/css" href="<c:url value="/style/main.css"/>">
    <title>The Project AURA Data Store</title>
  </head>
  <body>
    <%@include file="/WEB-INF/jspf/header.jspf"%>
    <%@include file="/WEB-INF/jspf/sidebar.jspf"%>
    <div class="main">
      <div class="mainTitle"><img src="images/tklogo.png"></div>
      <div class="sectionTitle">The Heart of AURA</div>
      <div class="regularTxt">
	<p>
	  The Project AURA Data Store was designed to be a
	  distributed, scalable, reliable data store that could be
	  used as a repository for item data and metadata as well as
	  user information and attention data that could be used by
	  recommenders for various item types.  Our aim is to be able
	  to store information about millions of items and potentially
	  billions of attention data points.
	</p>
	<p>
	  The AURA Data Store is composed of three parts:
	</p>

	<ul>
	  <li>One or more <em>data store heads</em>. A data store head
	    will be used by clients of the data store to write
	    data to the store and run queries against the stored data.</li>
	  <li>A number of <em>partition clusters</em>.  A partition
	    cluster represents a partition of the data held by the
	    store.  The data is partitioned via a hash function.</li>
	  <li>A number of <em>replicants</em>.  A replicant is
	    responsible for actually storing and searching the data in
	    the data store.  A single replicant is made up of a
	    key-value store and a search index built using
	    the <a href="http://minion.dev.java.net">Minion search
	      engine</a>.  A single partition of the data will be stored
	    in multiple replicants to provide reliability.</li>
	</ul>

	<div align="center">
	<img src="images/ds.png"/>
	</div>

	<p>
	  One of the key features of the Data Store is that it is
	  meant to be able to maintain and to grow itself.  When a
	  node in the Data Store goes down, the system recognizes that
	  this has happened and brings the node back up.
	</p>
	<p>
	  Our current approach to deploying the data store was
	  developed in the context of the Caroline grid.  Currently
	  we're hosting a 16 replicant data store that is capable of
	  handling more than 14,000 concurrent users performing
	  <a href="http://music.tastekeeper.com">typical music
	  recommendation tasks</a> with sub-500ms response times at
	  the client.
	</p>
      </div>
    </div>
  </body>
</html>
