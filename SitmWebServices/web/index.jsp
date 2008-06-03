<%-- 
    Document   : index
    Created on : May 23, 2008, 6:40:41 AM
    Author     : plamere
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <h2>SITM Web Service</h2>
        <ul>    
            <li> <a href="FindSimilarArtist?name=weezer"> Artists Similar to Weezer </a>
            <li> <a href="FindSimilarArtist?name=beatles"> Artists Similar to The Beatles </a>
            <li> <a href="FindSimilarArtistTags?name=metal&max=100"> Similar metal tags </a>
            <li> <a href="FindSimilarArtist?key=aa7a2827-f74b-473c-bd79-03d065835cf7"> Artists Similar to Franz Ferdinand </a>
            <li> <a href="ArtistSearch?name=beat"> Artist search for 'beat' </a>
            <li> <a href="ArtistTagSearch?name=alt"> Tag search for 'alt' </a>
            <li> <a href="ArtistSocialTags?name=weezer"> Distinctive tags for weezer</a>
            <li> <a href="ArtistSocialTags?name=weezer&type=frequent"> Frequent tags for weezer</a>
        </ul>
    </body>
</html>
