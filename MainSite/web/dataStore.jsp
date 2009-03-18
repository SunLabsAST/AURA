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

	<p>
	  You can see how these parts are organized in the diagram
	  below.
	</p>

	<div align="center">
	<img src="images/ds.png"/>
	</div>

	<p>
	  A data store is started by starting each of its separate
	  components.  Each component of the data store finds the
	  components to which it should be connected using
	  a <a href="http://www.jini.org">Jini Service Registrar</a>.
	  For example, a partition cluster will search for data store
	  heads with which it should register itself.  There is no
	  global configuration for the data store, only configurations
	  for the individual pieces.
	</p>

	<p>
	  The data store is meant to maintain itself, so each
	  component of the data store continuously monitors the
	  service registrar, noting when components of the data store
	  are added or removed, and acting accordingly.  For example,
	  if a replicant sees that the partition cluster responsible
	  for it disappears and then re-appears, it will reconnect
	  itself to the partition cluster.  The Data Store relies on
	  the underlying grid infrastructure to help restart services
	  when they terminate unexpectedly.  All of this means that
	  failures are self-correcting for the most part.  Our aim is
	  a data store that is hard to shut off.
	</p>

	<p>
	  The data store is also meant to grow itself as necessary.
	  When a replicant begins to get too full (i.e., when queries
	  against the replicant start to take too long), the partition
	  cluster undertakes the task of splitting the replicant into
	  two new replicants.  Once a replicant has been split in two,
	  a new partition cluster can be started to manage the new
	  replicant.  All of this can be done while the Data Store is
	  under load, so it's never necessary to stop the Data Store
	  (and the clients using the Data Store!) to add more capacity.
	</p>

	<p>
	  To give you some idea of the scalability of the current Data
	  Store, <a href="http://music.tastekeeper.com">The Music
	  Explaura</a> is supported by a 16 replicant Data Store that
	  is capable of handling more than 14,000 concurrent users
	  performing typical music recommendation tasks with sub-500ms
	  response times at the client.
	</p>
      </div>
    </div>
    <%@include file="/WEB-INF/jspf/footer.jspf"%>
  </body>
</html>
