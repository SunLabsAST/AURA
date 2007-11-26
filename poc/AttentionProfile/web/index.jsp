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
        
        If you are a last.fm user, you can get a representation of your 
        music listening taste, in the form of an APML file. Simply use the 
        webservice:
        
        <p>
        
        http://TasteBroker.org/music/Your_last.fm_name
        
        <p>
        
        Some examples:
        <ul>
            <li><a href="music/lamere"> Paul's APML File</a>
            <li><a href="music/rj"> RJ's APML File</a>
            <li><a href="music/musicmobs"> Toby's APML File</a>
            <li><a href="music/ocelma"> Oscar's APML File</a>
        </ul>
        <p>
        Note that it can take a while to generate your APML file since it requires a number
        of queries to last.fm to collect your taste data.  We try to cache as much info as we can
        to make things quick, but if you have eclectic tastes, you might have to wait a minute 
        or so.
        <h2> What is this all about?</h2>
        This is an experiment to see how easy it is to generate a useful APML file from
        existing web services. 
        <p>
        We can combine this APML generator with other services that process
        APML.  For instance <a href="http://www.cluztr.com/"> Cluztr </a> provides
        some Javascript that will turn an APML file into a tag cloud.  For example, here's
        my musical interest tag cloud:
        
        <p>
        <script type="text/javascript">
            tagcloud_title = "Paul's last.fm music interests";
            apml_url = "http://research.sun.com:8080/AttentionProfile/music/lamere/";
        </script>
        <script language="javascript" type="text/javascript" src="http://www.cluztr.com/api/apml_tag_cloud.js"></script>
        <p>
        There are also some <a href="music/stats">usage stats</a>.
        <h2> History </h2>
        <ul>
            <li> November 23, 2007 - added support for explicit concepts
            <li> November 21, 2007 - initial revision
        </ul>
        <hr>
        <center> 
            <br>TasteBroker.org v.1 - Powered by APML, Cluztr, Last.fm, and Sun Microsystems inc.
            <br> Send comments/feedback or complaints to Paul.Lamere@sun.com
        </center>
        
    </body>
</html>
