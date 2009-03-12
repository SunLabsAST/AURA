<%-- 
    Document   : main
    Created on : Mar 3, 2009, 2:49:09 PM
    Author     : ja151348
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">


<h1>Music Explaura</h1>
<br/>
The Music Explaura looks at the info in the "Favorite Music" section of your
profile.  It tries to identify each band listed by name, then builds a tag
cloud by combining the most distinctive terms that describe each band.
<br/><br/>
<!-- Facebook roundtrip: ${time} -->

<c:if test="${nomusic == true}">
It looks like you don't have any music listed in your profile, so we've
generated a tag cloud based solely on the band Coldplay.
</c:if>
<c:if test="${nomusic == false}">
Looking at your favorite music, we recognized the following bands:
<p>
<c:forEach items="${artists}" var="artist" varStatus="loop">
    ${artist.name}<c:if test="${!loop.last}">,</c:if>
</c:forEach>
</p>
</c:if>
<br/><br/>
<!-- Get artist time: ${auraTime} -->
<div style="width: 700px;" id="cloud">
    <span style="text-align: center; font-style: italic;">
    Loading your personal tag cloud<br/>
    <img src="${server}/image/loader.gif"/>
    </span>
</div>

<br/><br/>

<div style="float: right;">
<fb:if-section-not-added section="profile">
<fb:add-section-button section="profile" />
</fb:if-section-not-added>
</div>
<br/><br/><br/><br/>
<div style="padding: 3px; font-size: 10px; text-align: center; border: 1px solid #000000">
    <img src="${server}/image/sun_logo.png"/><br/>
    The Music Explaura is developed by <a href="http://research.sun.com/">Sun Labs</a> as part of The AURA Project.<br/>
    Data was used from <a href="http://musicbrainz.org">Musicbrainz</a> and <a href="http://last.fm">Last.fm</a>

</div>
<script type="text/javascript">
<!--
    var servletPath = "${server}/canvas";
    var fbSession = "${fbSession}";
    
    var artistIDs = [];
    <c:forEach items="${artists}" var="artist" varStatus="loop">
        artistIDs[${loop.index}] = "${artist.key}";
    </c:forEach>

        function displayCloud(data) {
            var thediv = document.getElementById('cloud');
            var cloud = document.createElement("div");
            for (var i=0; i < data.length; i++) {
                var curr = document.createElement("span");
                //var name = document.createTextNode(data[i].name);
                //curr.appendChild(name);
                curr.setTextValue(data[i].name + " ");
                var size = data[i].size + "px";
                if (i % 2 == 0) {
                    curr.setStyle({'fontSize': size, 'color': '#f8981d'});
                } else {
                    curr.setStyle({'fontSize': size, 'color': '#5382a1'});
                }
                cloud.appendChild(curr);
            }
            var kids = thediv.childNodes;
            if (kids != null) {
                for (var i = 0; i < kids.length; i++) {
                    thediv.removeChild(kids[i]);
                }
            }
            thediv.setTextValue("");
            thediv.appendChild(cloud);
        }

        function showDialog(title, msg) {
            dialog = new Dialog(Dialog.DIALOG_POP).
                showMessage(title, msg);
        }

        //
        // Run this when the page loads:
        var ajax = new Ajax();
        ajax.responseType = Ajax.JSON;
        ajax.ondone = displayCloud;
        var query = {"artists" : artistIDs.join(","),
                     "fbSession" : fbSession};
        ajax.post(servletPath + "/ajax/updateCloudFromArtistIDs", query);


//-->
</script>


<!--

<c:forEach var='parameter' items='${paramValues}'>
	<c:out value='${parameter.key}'/> =
	<c:forEach var='value' items='${parameter.value}'>
		<c:out value='${value}'/><br>
	</c:forEach>
</c:forEach>

-->