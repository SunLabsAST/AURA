<%-- 
    Document   : main
    Created on : Mar 3, 2009, 2:49:09 PM
    Author     : ja151348
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<style type="text/css">

</style>

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
<br/>

<!-- The "tabs" -->
<div style="border-bottom-width: 1px;border-bottom-style: solid;border-bottom-color: #898989;padding-bottom:5px;">
    <span class="inputbutton" id="cloudTab">My Cloud</span>&nbsp;&nbsp;
    <span class="inputbutton" id="compareTab">Compare</span>
</div>

<!-- Get artist time: ${auraTime} -->

<!-- The main display area (below the tabs) -->
<div style="width: 700px; min-height: 200px; padding: 5px;" id="mainSection">
</div>
<br/><br/>

<!-- The add to profile button -->
<div style="float: right;">
<fb:if-section-not-added section="profile">
<fb:add-section-button section="profile" />
</fb:if-section-not-added>
</div>

<br/><br/><br/><br/>

<!-- The credits -->
<div style="padding: 3px; font-size: 8px; text-align: center; border: 1px solid #222222">
    <img src="${server}/image/sun_logo.png"/><br/>
    The Music Explaura is developed by <a href="http://research.sun.com/">Sun Labs</a> as part of The AURA Project.<br/>
    Data was used from <a href="http://musicbrainz.org">Musicbrainz</a> and <a href="http://last.fm">Last.fm</a>

</div>

<!-- pre-rendered content to be used in the page -->
<fb:js-string var="friendPicker">
    <form id="friendSelector">
        <table><tr><td>
        Select a friend to compare to:
        </td><td>
        <fb:friend-selector idname="selected_id" />
        </td><td>
        <span class="inputbutton" id="compareGoBtn">Go</span>
        </td></tr></table>
    </form>
    <div id="compareResults">
    </div>
</fb:js-string>

<script type="text/javascript">
<!--
    var server = "${server}";
    var canvasPath = "${server}/canvas";
    var fbSession = "${fbSession}";
    
    var artistIDs = [];
    <c:forEach items="${artists}" var="artist" varStatus="loop">
        artistIDs[${loop.index}] = "${artist.key}";
    </c:forEach>

        //
        // clear out the contents of a div
        function clearDiv(thediv) {
            var kids = thediv.childNodes;
            if (kids != null) {
                for (var i = 0; i < kids.length; i++) {
                    thediv.removeChild(kids[i]);
                }
            }
            thediv.setTextValue("");
        }

        //
        // Creates the DOM that represents a cloud from cloud JSON data
        function getDOMForCloud(cloudData) {
            var cloud = document.createElement("div");
            for (var i=0; i < cloudData.length; i++) {
                var curr = document.createElement("span");
                curr.setTextValue(cloudData[i].name + " ");
                var size = cloudData[i].size;
                var orange = '#f8981d';
                var blue = '#5382a1';
                if (size < 0) {
                    orange = '#fbdcbd';
                    blue = '#baccd8';
                    size = size * -1;
                }
                var fontsize = cloudData[i].size + "px";

                if (i % 2 == 0) {

                    curr.setStyle({'fontSize': fontsize, 'color': orange});
                } else {
                    curr.setStyle({'fontSize': fontsize, 'color': blue});
                }
                cloud.appendChild(curr);
            }
            return cloud;
        }

        function displayCloudCallback(data) {
            var thediv = document.getElementById('mainSection');
            clearDiv(thediv);
            thediv.appendChild(getDOMForCloud(data));
        }

        function displayCompareCallback(data) {
            var thediv = document.getElementById('compareResults');
            clearDiv(thediv);
            //
            // Check for an error
            if (data.length == 1) {
                if (data[0].error != null) {
                    showDialog("Error", data[0].error);
                    return;
                }
            }
            thediv.appendChild(getDOMForCloud(data));
        }

        function showDialog(title, msg) {
            dialog = new Dialog(Dialog.DIALOG_POP).
                showMessage(title, msg);
        }

        function switchToLoader(thediv) {
            clearDiv(thediv);
            var loader = document.createElement("img");
            loader.setSrc(server + "/image/loader.gif");
            loader.setStyle({'position': 'relative', 'top': '90px', 'left': '340px'});
            thediv.appendChild(loader);
        }

        function cloudTabClicked() {
            var main = document.getElementById('mainSection');
            switchToLoader(main);
            fetchAndShowCloud();
        }

        function compareTabClicked() {
            //
            // Show the area where you can choose a friend
            var main = document.getElementById('mainSection');
            clearDiv(main);
            main.setInnerFBML(friendPicker);
            var goBtn = document.getElementById('compareGoBtn');
            goBtn.addEventListener('click', fetchAndShowCompare);
        }

        function fetchAndShowCompare() {
            var selector = document.getElementById('friendSelector');
            var selected = selector.serialize().selected_id;
            var tgt = document.getElementById('compareResults');
            switchToLoader(tgt);
            
            var ajax = new Ajax();
            ajax.responseType = Ajax.JSON;
            ajax.ondone = displayCompareCallback;
            var query = {"artists" : artistIDs.join(","),
                         "fbSession" : fbSession,
                         "friendUID" : selected};
            ajax.post(canvasPath + "/ajax/getCompareCloud", query);
        }

        function fetchAndShowCloud() {
            var ajax = new Ajax();
            ajax.responseType = Ajax.JSON;
            ajax.ondone = displayCloudCallback;
            var query = {"artists" : artistIDs.join(","),
                         "fbSession" : fbSession};
            ajax.post(canvasPath + "/ajax/updateCloudFromArtistIDs", query);
        }


        //
        // Run this when the page loads:
        var main = document.getElementById('mainSection');
        switchToLoader(main);
        fetchAndShowCloud();

        var cloudTab = document.getElementById("cloudTab");
        cloudTab.addEventListener('click', cloudTabClicked);

        var compareTab = document.getElementById("compareTab");
        compareTab.addEventListener('click', compareTabClicked);
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