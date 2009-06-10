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

.fakeTabCurrDisabled {
    color: #bbbbbb;
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

.fakeTabDisabled {
    color: #bbbbbb;
}

.fakeTabDisabled:hover {
    background-color: #d8dfea;
    border-top: 1px solid #d8dfea;
    border-right: 1px solid #d8dfea;
    border-left: 1px solid #d8dfea;
    border-bottom-width: 0pt;
    color: #bbbbbb;
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
    margin-left: auto;
    margin-right: auto;
    color: #888888;
    text-align: center;
}

.topMargin {
    margin-top: 12px;
}

.similarScrollWidget {
    border: 1px solid #7f93bc;
    height: 250px;
    overflow-y: auto;
    overflow-x: hidden;
    margin-bottom: 7px;
}

.similarResultsWidget {
    border: 1px solid #7f93bc;
    height: 250px;
    overflow-y: auto;
    overflow-x: hidden;
    margin-bottom: 7px;
}

.checkBoxLabel {
    vertical-align: middle;
}

.altBackground {
    background-color: #d8dfea;
}

.similarBandBox {
    padding: 3px;
    margin: 5px;
    text-align: center;
    display: inline-block;
    width: 82px;
    vertical-align: top;
}

</style>

<h1>Music Explaura</h1>
<br/>
The Music Explaura looks at the info in the "Favorite Music" section of your
profile.  It tries to identify each band listed by name, then builds a tag
cloud by combining the most distinctive terms that describe each band.
To explore bands and get customized recommendations, visit the full
<a href="http://music.tastekeeper.com">Music Explaura</a>.

<br/><br/><br/>

<!-- The "tabs" -->
<div style="border-bottom: 1px solid #d8dfea; padding-bottom:3px; padding-left:10px;">
    <span class="fakeTabCurr" id="cloudTab">My Cloud</span>&nbsp;
    <span class="fakeTab" id="similarTab">Similar</span>&nbsp;
    <span class="fakeTab" id="compareTab">Compare</span>&nbsp;
    <span class="fakeTab" id="shareTab">Share</span>
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
    <!-- <a href="http://www.sun.com/"><img src="${server}/image/sun_logo.png"/></a><br/> -->
    <table><tr><td>
    <a href="http://research.sun.com"><img src="${server}/image/sponsored_sl.6.png"/></a><br/>
    </td><td width="100%" style="text-align: center;">
    The Music Explaura is developed by <a href="http://research.sun.com/">Sun
    Labs</a> as part of <a href="http://www.tastekeeper.com/">The AURA Project</a>.<br/>
    Data was used from <a href="http://musicbrainz.org">Musicbrainz</a> and <a href="http://last.fm">Last.fm</a><br/>
    </td></tr></table>
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
    <div id="compareResults" style="min-height: 175px">
    </div>
</fb:js-string>

<fb:js-string var="shareWidget">
    <fb:request-form action="http://apps.facebook.com/musicexplaura" type="Music Explaura"
                     content="Use the Music Explaura to add your personal music
                             tag cloud to your profile and compare your taste
                             in music with your friends' taste.  Make sure you
                             have your favorite bands listed in the &quot;Favorite
                             Music&quot; section of your profile.
                             <fb:req-choice
                             url=&quot;http://apps.facebook.com/musicexplaura&quot;
                             label=&quot;Explore!&quot;>">
        <fb:multi-friend-selector actiontext="Select the friends to send a Music Explaura invitation to (those who have already used it are not listed):"
                                  showborder="false"
                                  exclude_ids="${friendAppUsers}"
                                  bypass="cancel"
                                  email_invite="false"/>
    </fb:request-form>
</fb:js-string>

<fb:js-string var="similarWidget">
    <form id="similarSelector">
        <table>
        <tr><td colspan="2" width="80%">
            <div class="infoText">Check band names on the left to see similar bands
            on the right.  Tags for each selected band are combined to create the
            similar band list.  Selecting too many may not be a great idea.
            Click band names to explore further.</div></td></tr>
        <tr>
        <td width="25%">
            <div class="similarScrollWidget" id="similarCheckList"></div>
        </td>
        <td width="75%">
            <div class="similarResultsWidget" id="similarResults"></div>
        </td>
        </tr>
        <tr>
        <td><span class="controlBtn" id="similarGoBtn">Show Similar Bands</span></td>
        </tr>
        </table>
    </form>
</fb:js-string>

<script type="text/javascript">
<!--
    var server = "${server}";
    var canvasPath = "${server}/canvas";
    var fbSession = "${fbSession}";
    var fbUID = "${fbUID}";

    var artistItems = [];
    <c:forEach items="${artistItems}" var="artist" varStatus="loop">
        artistItems[${loop.index}] = {name : "${artist.name}",
                                      key : "${artist.key}"};
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
    // This ridiculous function exists because its scope will preserve the
    // tagname parameter when the anonymous inner function here is given
    // to an event handler.
    function getLaunchWmeForTag(tagName) {
        return function () {
            //
            // We want to do this:
            //window.open("http://music.tastekeeper.com/#tag:artist-tag:" + tagName,
            //            "wme");
            // but we can't because of facebook, so instead we need to do this:
            var f = document.createElement("form");
            f.setTarget("wme");
            f.setMethod("get");
            f.setAction("http://music.tastekeeper.com/#tag:" + tagName);
            main.appendChild(f);
            f.submit();
        };
    }

    //
    // Creates the DOM that represents a cloud from cloud JSON data
    function getDOMForCloud(cloudData, cloudDesc) {
        var cloud = document.createElement("div");
        for (var i=0; i < cloudData.length; i++) {
            var curr = document.createElement("span");
            curr.setTextValue(cloudData[i].name + " ");
            curr.setStyle("cursor", "pointer");
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

            //
            // Make a clickable link to the WME
            curr.addEventListener('click', getLaunchWmeForTag(cloudData[i].key));
            cloud.appendChild(curr);
        }

        if (cloudDesc != null && cloudDesc.length > 0) {
            //
            // Show the cloud description
            var desc = document.createElement("div");
            desc.setTextValue(cloudDesc);
            desc.setClassName("infoText");
            desc.addClassName("topMargin");
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
            enableTabs();
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
        enableTabs();
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
        enableTabs();
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
        enableTabs();
    }

    function displaySimilarCallback(data) {
        var thediv = document.getElementById("similarResults");
        clearDiv(thediv);

        //
        // Check for an error
        var status = data.shift();
        if (status.error != null) {
            showDialog("Oops", status.error);
            clearDiv(thediv);
            enableTabs();
            return;
        }

        //
        // Display the results.  second arg is an array of artist objects
        var artists = data.shift();
        for (var i =0; i < artists.length; i++) {
            var artist = artists[i];
            var box = document.createElement("div");
            box.setClassName("similarBandBox");
            var link = document.createElement("a");
            link.setHref(artist.wmeLink);
            link.setTarget("wme");
            var thumb = document.createElement("img");
            thumb.setSrc(artist.thumbURL);
            thumb.setStyle({width : '75px', height : '75px'});
            link.appendChild(thumb);
            link.appendChild(document.createElement("br"));
            var text = document.createElement("span");
            text.setTextValue(artist.name);
            link.appendChild(text);
            box.appendChild(link);
            thediv.appendChild(box);
        }
        enableTabs();
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
        var top = Math.round((thediv.getClientHeight() / 2) - 16);
        var left = Math.round((thediv.getClientWidth() / 2) - 16);
        loader.setSrc(server + "/image/loader.gif");
        loader.setStyle({'position': 'relative', 'top': top + "px", 'left': left + "px"});
        thediv.appendChild(loader);
    }

    function cloudTabClicked() {
        //
        // Switch "selected" tab
        setSelectedTab(cloudTab);

        //
        // Fetch the cloud data
        var main = document.getElementById('mainSection');
        switchToLoader(main);
        fetchAndShowCloud();

    }

    function compareTabClicked() {
        //
        // Switch "selected" tab
        setSelectedTab(compareTab);

        //
        // Show the area where you can choose a friend
        main.setInnerFBML(friendPicker);
        var goBtn = document.getElementById('compareGoBtn');
        goBtn.addEventListener('click', fetchAndShowCompare);
        var seeBtn = document.getElementById('seeOtherBtn');
        seeBtn.addEventListener('click', fetchAndShowFriendCloud);
    }

    function shareTabClicked() {
        //
        // Switch the "selected" tab
        setSelectedTab(shareTab);

        main.setInnerFBML(shareWidget);
    }

    function similarTabClicked() {
        //
        // Switch the "selected" tab
        setSelectedTab(similarTab);

        //
        // Fill in the tab content.  Create a list of bands and check
        // boxes, and a panel to show results.
        main.setInnerFBML(similarWidget);
        var listDiv = document.getElementById('similarCheckList');
        clearDiv(listDiv);
        var table = document.createElement("table");
        table.setStyle('borderSpacing', "0");
        for (var i = 0; i < artistItems.length; i++) {
            var row = document.createElement("tr");
            if (i % 2 == 1) {
                row.addClassName("altBackground");
            }
            var cell = document.createElement("td");
            cell.setStyle('verticalAlign', 'middle');
            var cb = document.createElement("input");
            cb.setType("checkbox");
            cb.setName("similarCheckBoxes");
            cb.setId("scb-" + i);
            cb.setValue(i);
            if (i == 0) {
                cb.setChecked("true");
            }
            cell.appendChild(cb);
            row.appendChild(cell);
            cell = document.createElement("td");
            var text = document.createElement("a").setTextValue(artistItems[i].name);
            text.setHref("http://music.tastekeeper.com/#artist:" + artistItems[i].key);
            text.setTarget("wme");
            cell.appendChild(text);
            row.appendChild(cell);
            table.appendChild(row);
        }
        listDiv.appendChild(table);

        var btn = document.getElementById('similarGoBtn');
        btn.addEventListener('click', fetchAndShowSimilar);

        //
        // If we have any artists, send the request to get similar artists
        if (artistItems.length > 0) {
            fetchAndShowSimilar();
        }
    }


    function setSelectedTab(selectedTab) {
        currentTab.setClassName("fakeTab");
        selectedTab.setClassName("fakeTabCurr");
        currentTab = selectedTab;

        //
        // Clear the main and invite areas
        var main = document.getElementById('mainSection');
        clearDiv(main);
        var inv = document.getElementById("inviteArea");
        clearDiv(inv);

        //
        // Clear the add to profile area and the WME link
        var profile = document.getElementById("addToProfileArea");
        clearDiv(profile);
        var wmeLink = document.getElementById("wmeLink");
        clearDiv(wmeLink);
    }

    function enableTabs() {
        cloudTab.addEventListener('click', cloudTabClicked);
        cloudTab.removeClassName("fakeTabDisabled");
        compareTab.addEventListener('click', compareTabClicked);
        compareTab.removeClassName("fakeTabDisabled");
        shareTab.addEventListener('click', shareTabClicked);
        shareTab.removeClassName("fakeTabDisabled");
        similarTab.addEventListener('click', similarTabClicked);
        similarTab.removeClassName("fakeTabDisabled");

        currentTab.removeClassName("fakeTabCurrDisabled");
    }

    function disableTabs() {
        cloudTab.removeEventListener('click', cloudTabClicked);
        cloudTab.addClassName("fakeTabDisabled")
        compareTab.removeEventListener('click', compareTabClicked);
        compareTab.addClassName("fakeTabDisabled")
        shareTab.removeEventListener('click', shareTabClicked);
        shareTab.addClassName("fakeTabDisabled")
        similarTab.removeEventListener('click', similarTabClicked);
        similarTab.addClassName("fakeTabDisabled")

        currentTab.removeClassName("fakeTabDisabled");
        currentTab.addClassName("fakeTabCurrDisabled");
    }

    function ajaxError() {
        var thediv = document.getElementById('mainSection');
        clearDiv(thediv);
        showDialog("Error", "Sorry, an error has occurred.  Please try again later.");
        enableTabs();
    }

    /*
     * Fetch the bands similar to the checked bands and show then in the
     * appropriate region as links to the WME
     */
    function fetchAndShowSimilar() {
        disableTabs();
        //
        // Get the selected bands.  There should be a checkbox for each artist.
        // Iterate to check the value for each one.
        var ids = [];
        for (var i = 0; i < artistItems.length; i++) {
            var checkbox = document.getElementById("scb-" + i);
            if (checkbox.getChecked() == true) {
                ids.push(artistItems[i].key);
            }
        }

        //
        // Make sure we got at least one band
        if (ids.length == 0) {
            showDialog("Error", "Please select at least one band from the left column");
            return;
        }

        //
        // Clear the results panel and set it to loading
        var res = document.getElementById("similarResults");
        switchToLoader(res);

        //
        // Make the AJAX call
        var ajax = new Ajax();
        ajax.responseType = Ajax.JSON;
        ajax.ondone = displaySimilarCallback;
        ajax.onerror = function() {
            var thediv = document.getElementById('similarResults');
            clearDiv(thediv);
            showDialog("Error", "Sorry, an error has occurred.  Please try again later.");
        };
        var query = {'artists' : ids.join("+")};
        ajax.post(canvasPath + "/ajax/getSimilarBands", query);
    }

    /*
     * Shows a comparison cloud between the logged in user and a selected
     * friend.
     */
    function fetchAndShowCompare(friendID) {
        disableTabs();
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
        disableTabs();
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
        disableTabs();
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

    //
    // Get handles to each of the "tab" objects
    var cloudTab = document.getElementById("cloudTab");
    var compareTab = document.getElementById("compareTab");
    var shareTab = document.getElementById("shareTab");
    var similarTab = document.getElementById("similarTab");

    var currentTab = cloudTab;

    <c:if test="${compareTo != null}">
    //
    // Switch to compare tab
    compareTabClicked();
    fetchAndShowCompare(<c:out value="${compareTo}" />);
    </c:if>
    <c:if test="${compareTo == null}">
    fetchAndShowCloud();
    </c:if>


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