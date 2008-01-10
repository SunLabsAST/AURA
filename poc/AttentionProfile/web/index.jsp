<%-- 
    Document   : index
    Created on : Nov 15, 2007, 8:21:40 PM
    Author     : plamere
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>TasteBroker.org </title>
    </head>
    <body>
        <h2>Welcome to TasteBroker.org</h2>
        TasteBroker is an experimental service that will generate a portable 
        representation of your taste data (in the form of an
        <a href="http://www.apml.org">APML</a> file) from various web sources.
        
        <h3> APML from last.fm</h3>
        If you are a last.fm user, you can get a representation of your 
        music listening taste, in the form of an APML file. Simply use the 
        webservice:
        
        <p>
        
        http://TasteBroker.org/apml/music/Your_last.fm_name
        
        <p>
        
        Some examples:
        <ul>
            <li><a href="apml/last.fm/lamere"> Paul's APML File</a>
            <li><a href="apml/last.fm/rj"> RJ's APML File</a>
            <li><a href="apml/last.fm/musicmobs"> Toby's APML File</a>
            <li><a href="apml/last.fm/ocelma"> Oscar's APML File</a>
        </ul>
        <p>
            Try it yourself:
        <p>
            <font size="+2">
             http://TasteBroker.org/apml/last.fm/<input type="text" 
             onchange="window.location = 'apml/last.fm/' + this.value;" />
            </font>
        <p>
        Note that it can take a while to generate your APML file since it requires a number
        of queries to last.fm to collect your taste data.  We try to cache as much info as we can
        to make things quick, but if you have eclectic tastes, you might have to wait a minute 
        or so.

        <h3> APML from Pandora</h3>
        If you are a Pandora user, you can get a representation of your 
        music listening taste, in the form of an APML file. Simply use the 
        webservice:
        
        <p>
        
        http://TasteBroker.org/apml/pandora/Your_Pandora_name
        
        <p>
        
        Some examples:
        <ul>
            <li><a href="apml/pandora/paul.lamere"> Paul's APML File</a>
            <li><a href="apml/pandora/tim"> Tim Westergren's APML File</a>
            <li><a href="apml/pandora/tconrad"> Tom Conrad's APML File</a>
        </ul>
        <p>
            Try it yourself:
        <p>
            <font size="+2">
             http://TasteBroker.org/apml/pandora/<input type="text" 
             onchange="window.location = 'apml/pandora/' + this.value;" />
            </font>
        <p>
        Note that it can take a while to generate your APML file since it requires a number
        of queries to Pandora and last.fm to collect your taste data.  We try to cache as much info as we can
        to make things quick, but if you have eclectic tastes, you might have to wait a minute 
        or so.

        <h3> APML from del.icio.us</h3>
        If you are a del.icio.us user, you can get a representation of your 
        web browsing taste, in the form of an APML file. Simply use the 
        webservice:
        
        <p>
        
        http://TasteBroker.org/apml/web/Your_delicious_name
        
        <p>
        
        Some examples:
        <ul>
            <li><a href="apml/web/plamere"> Paul's APML File</a>
            <li><a href="apml/web/toby"> Toby's APML File</a>
            <li><a href="apml/web/tristanf"> Tristan's APML File</a>
        </ul>
        <p>
            Try it yourself:
        <p>
            <font size="+2">
             http://TasteBroker.org/apml/web/<input type="text" 
             onchange="window.location = 'apml/web/' + this.value;" />
            </font>
        <p>
        <p>
        <h2> What is this all about?</h2>
        This is an experiment to see how easy it is to generate a useful APML file from
        existing web services. 
        <p>
        We can combine this APML generator with other services that process
        APML.  For instance <a href="http://www.cluztr.com/"> Cluztr </a> provides
        some Javascript that will turn an APML file into a tag cloud.  For example, here's
        a tag cloud generated from my APML file:
        
        <p>
        <script type="text/javascript">
            tagcloud_title = "Paul's web interests";
            apml_url = "http://research.sun.com:8080/AttentionProfile/apml/web/plamere/";
        </script>
        <script language="javascript" type="text/javascript" src="http://www.cluztr.com/api/apml_tag_cloud.js"></script>
        <p>
            <!--
        <p>
        <script type="text/javascript">
            tagcloud_title = "Paul's last.fm music interests";
            apml_url = "http://research.sun.com:8080/AttentionProfile/apml/last.fm/lamere/";
        </script>
        <script language="javascript" type="text/javascript" src="http://www.cluztr.com/api/apml_tag_cloud.js"></script>
        -->
        <p>
        There are also some <a href="apml/stats">usage stats</a>.
        <h2> History </h2>
        <ul>
            <li> January  10, 2008 - Refactoring to support APML loading and recommendation
            <li> December  5, 2007 - Added Pandora support
            <li> December  2, 2007 - timezone, concept normalization fixes
            <li> November 30, 2007 - added del.icio.us support
            <li> November 23, 2007 - added support for explicit concepts
            <li> November 21, 2007 - initial revision
        </ul>
        <hr>
        <center> 
            <br>TasteBroker.org v.6 - Powered by APML, Cluztr, Del.icio.us, Last.fm, Pandora and Sun Microsystems inc.
            <br> Send comments/feedback or complaints to Paul.Lamere@sun.com
        </center>
    </body>
</html>