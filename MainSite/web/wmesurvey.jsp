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
    <link rel="icon" type="image/png" href="images/tkfavicon.png"/>
    <title>Music Explaura Survey</title>
    <style type="text/css">
table {
    width: 100%;
}

td {
    width: 20%;
    text-align: center;
    vertical-align: middle;
}

td.buttons {
    padding-bottom: 4px;
}
    </style>
  </head>
  <body>
    <%@include file="/WEB-INF/jspf/header.jspf"%>
    <%@include file="/WEB-INF/jspf/sidebar.jspf"%>
    <div class="main">
      <div class="mainTitle"><img src="images/tklogo.png"></div>
      <c:if test="${alreadyTaken == false}">
      <div class="sectionTitle">Music Explaura Survey</div>
      <div class="regularTxt">
          Thank you for your interest in the Music Explaura.  To help us learn
          about how you used and liked or disliked the Explaura, please consider
          completing the following brief survey.
          <form action="wmesurveysubmit">
          <ol>
              <li>Overall, please rate your experience with the Music Explaura website.
              <table border="1">
              <tr>
                  <td width="20%">Not enjoyable at all</td>
                  <td width="20%"></td>
                  <td width="20%"></td>
                  <td width="20%"></td>
                  <td width="20%">Highly enjoyable</td>
              </tr>
              <tr>
                  <td width="20%" class="buttons"><input type="radio" name="question1" value="1"/></td>
                  <td width="20%" class="buttons"><input type="radio" name="question1" value="2"/></td>
                  <td width="20%" class="buttons"><input type="radio" name="question1" value="3"/></td>
                  <td width="20%" class="buttons"><input type="radio" name="question1" value="4"/></td>
                  <td width="20%" class="buttons"><input type="radio" name="question1" value="5"/></td>
              </tr>
              </table>
              </li>
              <br/>
              <li>
                  How likely are you to recommend this website to a friend?
              <table border="1">
              <tr>
                  <td width="20%">Not likely at all</td>
                  <td width="20%"></td>
                  <td width="20%"></td>
                  <td width="20%"></td>
                  <td width="20%">Highly likely</td>
              </tr>
              <tr>
                  <td width="20%" class="buttons"><input type="radio" name="question2" value="1"/></td>
                  <td width="20%" class="buttons"><input type="radio" name="question2" value="2"/></td>
                  <td width="20%" class="buttons"><input type="radio" name="question2" value="3"/></td>
                  <td width="20%" class="buttons"><input type="radio" name="question2" value="4"/></td>
                  <td width="20%" class="buttons"><input type="radio" name="question2" value="5"/></td>
              </tr>
              </table>
              </li>
              <br/>
              <li>
                  How likely are you to use the Explaura website as a resource in the future?
              <table border="1">
              <tr>
                  <td width="20%">Not likely at all</td>
                  <td width="20%"></td>
                  <td width="20%"></td>
                  <td width="20%"></td>
                  <td width="20%">Highly likely</td>
              </tr>
              <tr>
                  <td width="20%" class="buttons"><input type="radio" name="question3" value="1"/></td>
                  <td width="20%" class="buttons"><input type="radio" name="question3" value="2"/></td>
                  <td width="20%" class="buttons"><input type="radio" name="question3" value="3"/></td>
                  <td width="20%" class="buttons"><input type="radio" name="question3" value="4"/></td>
                  <td width="20%" class="buttons"><input type="radio" name="question3" value="5"/></td>
              </tr>
              </table>
              </li>
              <br/>
              <li>
                  To what extent did you understand the reasoning behind the music recommendations?
              <table border="1">
              <tr>
                  <td width="20%">I didn't understand at all</td>
                  <td width="20%"></td>
                  <td width="20%"></td>
                  <td width="20%"></td>
                  <td width="20%">I understood completely</td>
              </tr>
              <tr>
                  <td width="20%" class="buttons"><input type="radio" name="question4" value="1"/></td>
                  <td width="20%" class="buttons"><input type="radio" name="question4" value="2"/></td>
                  <td width="20%" class="buttons"><input type="radio" name="question4" value="3"/></td>
                  <td width="20%" class="buttons"><input type="radio" name="question4" value="4"/></td>
                  <td width="20%" class="buttons"><input type="radio" name="question4" value="5"/></td>
              </tr>
              </table>
              </li>
              <br/>
              <li>
                  How many new bands or performers did you discover during your time on the Explaura site today?
              <table border="1">
              <tr>
                  <td width="20%">0</td>
                  <td width="20%">1</td>
                  <td width="20%">2</td>
                  <td width="20%">3-4</td>
                  <td width="20%">5 or more</td>
              </tr>
              <tr>
                  <td width="20%" class="buttons"><input type="radio" name="question5" value="1"/></td>
                  <td width="20%" class="buttons"><input type="radio" name="question5" value="2"/></td>
                  <td width="20%" class="buttons"><input type="radio" name="question5" value="3"/></td>
                  <td width="20%" class="buttons"><input type="radio" name="question5" value="4"/></td>
                  <td width="20%" class="buttons"><input type="radio" name="question5" value="5"/></td>
              </tr>
              </table>
              </li>
              <br/>
              <li>
                  How would you rate the relevance of the recommendations?
              <table border="1">
              <tr>
                  <td width="20%">Not relevant at all</td>
                  <td width="20%"></td>
                  <td width="20%"></td>
                  <td width="20%"></td>
                  <td width="20%">Highly relevant</td>
              </tr>
              <tr>
                  <td width="20%" class="buttons"><input type="radio" name="question6" value="1"/></td>
                  <td width="20%" class="buttons"><input type="radio" name="question6" value="2"/></td>
                  <td width="20%" class="buttons"><input type="radio" name="question6" value="3"/></td>
                  <td width="20%" class="buttons"><input type="radio" name="question6" value="4"/></td>
                  <td width="20%" class="buttons"><input type="radio" name="question6" value="5"/></td>
              </tr>
              </table>
              </li>
              <br/>
              <li>
                  To what extent were the tag clouds helpful in guiding your exploration of new music?
              <table border="1">
              <tr>
                  <td width="20%">Not helpful at all</td>
                  <td width="20%"></td>
                  <td width="20%"></td>
                  <td width="20%"></td>
                  <td width="20%">Very helpful</td>
              </tr>
              <tr>
                  <td width="20%" class="buttons"><input type="radio" name="question7" value="1"/></td>
                  <td width="20%" class="buttons"><input type="radio" name="question7" value="2"/></td>
                  <td width="20%" class="buttons"><input type="radio" name="question7" value="3"/></td>
                  <td width="20%" class="buttons"><input type="radio" name="question7" value="4"/></td>
                  <td width="20%" class="buttons"><input type="radio" name="question7" value="5"/></td>
              </tr>
              </table>
              </li>
              <br/>
              <li>
                  How easy is it to discover and learn to use Explaura’s features and functionality? (tag clouds, steering)
              <table border="1">
              <tr>
                  <td width="20%">Very difficult</td>
                  <td width="20%"></td>
                  <td width="20%"></td>
                  <td width="20%"></td>
                  <td width="20%">Very easy</td>
              </tr>
              <tr>
                  <td width="20%" class="buttons"><input type="radio" name="question8" value="1"/></td>
                  <td width="20%" class="buttons"><input type="radio" name="question8" value="2"/></td>
                  <td width="20%" class="buttons"><input type="radio" name="question8" value="3"/></td>
                  <td width="20%" class="buttons"><input type="radio" name="question8" value="4"/></td>
                  <td width="20%" class="buttons"><input type="radio" name="question8" value="5"/></td>
              </tr>
              </table>
              </li>
              <br/>
              <li>
                  Please comment on any aspects of the site you found to be particularly satisfying or positive
                  <textarea cols="80" rows="4" name="question9"></textarea>
              </li>
              <br/>
              <li>
                  Please comment on any aspects of the site you found to be particularly problematic or negative
                  <textarea cols="80" rows="4" name="question10"></textarea>
              </li>
              <br>
              <input type="submit" value="Submit">
          </ol>
          </form>
          <br>
      </div>
      </c:if>
      <c:if test="${alreadyTaken == true}">
          <c:choose>
          <c:when test="${submitted == false}">
              <div class="sectionTitle">Survey Completed</div>
              <div class="regularTxt">
                  You have already completed the survey.  Thank you for your
                  participation!
              </div>
          </c:when>

          <c:when test="${submitted == true}">
              <div class="sectionTitle">Survey Completed</div>
              <div class="regularTxt">
                  Thank you for participating in our survey!
                  <p/>
                  <ul>
                      <li>Return to the <a href="http://music.tastekeeper.com">Music Explaura</a></li>
                  </ul>
              </div>
          </c:when>
          </c:choose>
      </c:if>
    </div>
    <%@include file="/WEB-INF/jspf/footer.jspf"%>
  </body>
</html>