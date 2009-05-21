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

.fakeTabCurr {
    border-top: 1px solid #d8dfea;
    border-right: 1px solid #d8dfea;
    border-left: 1px solid #d8dfea;
    border-bottom: 1px solid #ffffff;
    font-size: 13px;
    font-weight: bold;
    display: inline;
    padding-top: 3px;
    padding-right: 11px;
    padding-bottom: 3px;
    padding-left: 11px;
    white-space: nowrap;
}

.fakeTab {
    background-color: #d8dfea;
    color: #3b5998;
    border-top: 1px solid #d8dfea;
    border-right: 1px solid #d8dfea;
    border-left: 1px solid #d8dfea;
    border-bottom-width: 0pt;
    font-size: 13px;
    font-weight: bold;
    display: inline;
    padding-top: 3px;
    padding-right: 11px;
    padding-bottom: 3px;
    padding-left: 11px;
    white-space: nowrap;
}

.fakeTab:hover {
    background-color: #627aad;
    border-top-color: #627aad;
    border-right-color: #627aad;
    border-bottom-color: #627aad;
    border-left-color: #627aad;
    color: #ffffff;
}

.controlBtn {
    border: 1px solid #7f93bc;
    padding: 3px 15px 3px 15px;
    color: #3b5998;
}

.controlBtn:hover {
    color: #ffffff;
    background-color: #3b5998;
}

.infoText {
    border: 1px solid #d8dfea;
    width: 600px;
    padding: 4px;
    margin-top: 12px;
    margin-left: auto;
    margin-right: auto;
    color: #888888;
    text-align: center;
}

</style>

<h1>Music Explaura</h1>
<br/>
The Music Explaura looks at the info in the "Favorite Music" section of your
profile.  It tries to identify each band listed by name, then builds a tag
cloud by combining the most distinctive terms that describe each band.
<br/><br/><br/>

<!-- The "tabs" -->
<div style="border-bottom: 1px solid #d8dfea; padding-bottom:3px; padding-left:10px;">
    <span class="fakeTabCurr" id="cloudTab">My Cloud</span>&nbsp;
    <span class="fakeTab" id="compareTab">Compare</span>
</div>

<!-- The main display area (below the tabs) -->
<div style="width: 700px; min-height: 200px; padding: 5px;" id="mainSection">
</div>
<br/>

<!-- Link to invite user to use the app -->
<div style="float:right; clear: right;" id="inviteArea">
</div>
<!-- The add to profile button -->
<div style="float: right; clear: right;" id="addToProfileArea">
</div>

<!-- Link to the WME for steering and recommendations -->
<div id="wmeLink">
</div>

<br/><br/><br/><br/>

<!-- The credits -->
<div style="padding: 3px; font-size: 8px; text-align: center; border: 1px solid #222222">
    <a href="http://www.sun.com/"><img src="${server}/image/sun_logo.png"/></a><br/>
    The Music Explaura is developed by <a href="http://research.sun.com/">Sun
    Labs</a> as part of <a href="http://www.tastekeeper.com/">The AURA Project</a>.<br/>
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
        <span class="controlBtn" id="compareGoBtn">Compare</span>
        </td><td>
        <span class="controlBtn" id="seeOtherBtn">See Cloud</span>
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
    var fbUID = "${fbUID}";
    
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
    function getDOMForCloud(cloudData, cloudDesc) {
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
            var fontsize = size + "px";

            if (i % 2 == 0) {

                curr.setStyle({'fontSize': fontsize, 'color': orange});
            } else {
                curr.setStyle({'fontSize': fontsize, 'color': blue});
            }
            cloud.appendChild(curr);
        }

        if (cloudDesc != null && cloudDesc.length > 0) {
            //
            // Show the cloud description
            var desc = document.createElement("div");
            desc.setTextValue(cloudDesc);
            desc.setClassName("infoText");
            var container = document.createElement("div");
            container.appendChild(cloud);
            container.appendChild(desc);
            cloud = container;
        }
        return cloud;
    }

    function displayCloudCallback(data) {
        var thediv = document.getElementById('mainSection');
        clearDiv(thediv);
        //
        // Check for an error
        var status = data.shift();
        if (status.error != null) {
            showDialog("Oops", status.error);
            return;
        }

        //
        // Show the cloud
        var artistStr = "Based on: " + status.artists;
        if (status.hasmusic == "false") {
            artistStr = "We couldn't identify any bands in your favorite " +
                "music so a cloud is being displayed based on " + status.artists;
        }
        thediv.appendChild(getDOMForCloud(data, artistStr));

        //
        // Show a link to the WME
        var wmeLink = document.getElementById("wmeLink");
        wmeLink.setInnerFBML(status.fbml_steerLink);

        //
        // Put in the add-to-profile button if relevant
        var profile = document.getElementById("addToProfileArea");
        profile.setInnerFBML(status.fbml_profile);
    }

    function displayCompareCallback(data) {
        var thediv = document.getElementById('compareResults');
        clearDiv(thediv);
        //
        // Check for an error
        var status = data.shift();
        if (status.error != null) {
            showDialog("Oops", status.error);
        } else {
            var infoText = "A comparison between your taste and " +
            status.friendName + "'s.  Tags unique to you are first, then your " +
            " shared tags, then tags that only " + status.friendName + " has."
            thediv.appendChild(getDOMForCloud(data, infoText));
        }
        
        //
        // Should we add an invite button?
        if (status.isAppUser == "false") {
            var inv = document.getElementById("inviteArea");
            inv.setInnerFBML(status.fbml_invite);
        }
    }

    function displayFriendCloudCallback(data) {
        var thediv = document.getElementById('compareResults');
        clearDiv(thediv);
        //
        // Check for an error
        var status = data.shift();
        if (status.error != null) {
            showDialog("Oops", status.error);
        } else {
            thediv.appendChild(getDOMForCloud(data, "Based on: " +
                status.friendArtists));
        }

        //
        // Should we add an invite button?
        if (status.isAppUser == "false") {
            var inv = document.getElementById("inviteArea");
            inv.setInnerFBML(status.fbml_invite);
        }
    }

    function showDialog(title, msg) {
        var dialog = new Dialog(Dialog.DIALOG_POP).
            showMessage(title, msg);
    }

    function getSelectedFriend() {
        var selector = document.getElementById('friendSelector');
        var selected = selector.serialize().selected_id;
        return selected;
    }

    function switchToLoader(thediv) {
        clearDiv(thediv);
        var loader = document.createElement("img");
        loader.setSrc(server + "/image/loader.gif");
        loader.setStyle({'position': 'relative', 'top': '90px', 'left': '340px'});
        thediv.appendChild(loader);
    }

    function cloudTabClicked() {
        //
        // Switch "selected" tab
        var cloudTab = document.getElementById("cloudTab");
        var compareTab = document.getElementById("compareTab");
        cloudTab.setClassName("fakeTabCurr");
        compareTab.setClassName("fakeTab");

        //
        // Fetch the cloud data
        var main = document.getElementById('mainSection');
        switchToLoader(main);
        fetchAndShowCloud();

        //
        // Clear the invite area
        var inv = document.getElementById("inviteArea");
        clearDiv(inv);
    }

    function compareTabClicked() {
        //
        // Switch "selected" tab
        var cloudTab = document.getElementById("cloudTab");
        var compareTab = document.getElementById("compareTab");
        cloudTab.setClassName("fakeTab");
        compareTab.setClassName("fakeTabCurr");

        //
        // Show the area where you can choose a friend
        var main = document.getElementById('mainSection');
        clearDiv(main);
        main.setInnerFBML(friendPicker);
        var goBtn = document.getElementById('compareGoBtn');
        goBtn.addEventListener('click', fetchAndShowCompare);
        var seeBtn = document.getElementById('seeOtherBtn');
        seeBtn.addEventListener('click', fetchAndShowFriendCloud);

        //
        // Clear the add to profile area and the WME link
        var profile = document.getElementById("addToProfileArea");
        clearDiv(profile);
        var wmeLink = document.getElementById("wmeLink");
        clearDiv(wmeLink);
    }

    function ajaxError() {
        var thediv = document.getElementById('mainSection');
        clearDiv(thediv);
        showDialog("Error", "Sorry, an error has occurred.  Please try again later.");
    }

    /*
     * Shows a comparison cloud between the logged in user and a selected
     * friend.
     */
    function fetchAndShowCompare(friendID) {
        //
        // Clear the invite area
        var inv = document.getElementById("inviteArea");
        clearDiv(inv);

        var selected;
        if (friendID.type != null) {
            selected = getSelectedFriend();
        } else {
            selected = friendID;
        }
        var tgt = document.getElementById('compareResults');
        switchToLoader(tgt);

        var ajax = new Ajax();
        ajax.responseType = Ajax.JSON;
        ajax.ondone = displayCompareCallback;
        ajax.onerror = ajaxError;
        var query = {"fbUID" : fbUID,
                     "fbSession" : fbSession,
                     "friendUID" : selected};
        ajax.post(canvasPath + "/ajax/getCompareCloud", query);
    }

    /*
     * Shows a cloud for the logged-in user and updates their profile FBML
     */
    function fetchAndShowCloud() {
        var ajax = new Ajax();
        ajax.responseType = Ajax.JSON;
        ajax.ondone = displayCloudCallback;
        ajax.onerror = ajaxError;
        var query = {"fbUID" : fbUID,
                     "fbSession" : fbSession};
        ajax.post(canvasPath + "/ajax/updateCloud", query);
    }

    /*
     * Shows a cloud (on the compare screen) representing the selected
     * friend's musical tastes
     */
    function fetchAndShowFriendCloud() {
        //
        // Clear the invite area
        var inv = document.getElementById("inviteArea");
        clearDiv(inv);

        var selected = getSelectedFriend();
        var tgt = document.getElementById('compareResults');
        switchToLoader(tgt);

        var ajax = new Ajax();
        ajax.responseType = Ajax.JSON;
        ajax.ondone = displayFriendCloudCallback;
        ajax.onerror = ajaxError;
        var query = {"fbSession" : fbSession,
                     "friendUID" : selected};
        ajax.post(canvasPath + "/ajax/getOtherCloud", query);
    }

    //
    // Run this when the page loads:
    var main = document.getElementById('mainSection');
    switchToLoader(main);
    <c:if test="${compareTo != null}">
    //
    // Switch to compare tab
    compareTabClicked();
    fetchAndShowCompare(<c:out value="${compareTo}" />);
    </c:if>
    <c:if test="${compareTo == null}">
    fetchAndShowCloud();
    </c:if>

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