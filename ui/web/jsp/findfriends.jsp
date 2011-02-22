<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>


<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
 
    <link href="css/reset.css" rel="stylesheet" type="text/css" media="screen,projection" />
    <link href="css/960.css" rel="stylesheet" type="text/css" media="screen,projection" />
    <link href="css/text.css" rel="stylesheet" type="text/css" media="screen,projection" />
    <link href="css/screen.css" rel="stylesheet" type="text/css" media="screen,projection" />
    <script type="text/javascript" src="js/global.js"></script>

    <title>Find Friends Page</title>
  </head>
  
  <script type="text/javascript">  
    function submitAction(actionType)
    {    
    	var action = document.getElementById('performAction');
  	  action.value = actionType;

  	  var form = document.getElementById('findfriendsfrm');

  	  form.submit();
  	  return true;    	
    } 
  </script>

  <body>
    <div class="container_12 clearfix">
      <div id="header">
        <div id="logo" class="grid_7">
          <a href="/">Chirper</a>
        </div>
        <ul id="nav" class="grid_5">
          <li><a href="usertimeline.htm">Home</a></li>
          <li><a href="publicpage.htm">Public</a></li>
          <li><a href="logout.htm">Sign out</a></li>
        </ul>
      </div>
      <div class="clear"></div>
  
      <div class="grid_9 alpha">
        <form:form id="findfriendsfrm" commandName="findfriendsCmd" >   
           
          <h2 class="grid_4 suffix_5">Find a Friend</h2>

          <spring:hasBindErrors name="findfriendsCmd">      
            <ul class="error">
              <c:forEach var="errMsgObj" items="${errors.allErrors}">
                <li class="error">
                  <spring:message message="${errMsgObj}"/>
                </li>
              </c:forEach>
            </ul>
          </spring:hasBindErrors> 		
        
          <div style="position: relative; left: 20px;">
            <ul class="grid_9 alpha">
              <li><label for="id_friendname">Enter&nbsp;Name:</label>
                <form:input id="id_friendname" path="friendName"  size="30" />
	            <input type="button" value="Add" class="button" onclick="submitAction('add');"/>
	            <input type="button" value="Done" class="button"  onclick="submitAction('done');"/>
              </li>
            </ul>
          </div>

          <h2 class="grid_8 suffix_5">Current Friends for: ${findfriendsCmd.userName}</h2>
          <ul id="timeline" class="grid_9 alpha">
            <c:forEach var="friendItem" items="${friends}" varStatus="friendStatus">
              <li>
                <a href="userchirps.htm?chirpUserId=<c:out value='${friendItem.userId}'/>" class="username"><c:out value="${friendItem.userName}"/></a>            
              </li>
            </c:forEach>
          </ul>

          <h2 class="grid_8 suffix_5">Followers for: ${findfriendsCmd.userName}</h2>
          <ul id="timeline" class="grid_9 alpha">
            <c:forEach var="followerItem" items="${followers}" varStatus="followerStatus">
              <li>
                <a href="userchirps.htm?chirpUserId=<c:out value='${followerItem.userId}'/>" class="username"><c:out value="${followerItem.userName}"/></a>            
              </li>
            </c:forEach>
          </ul>
     
          <form:hidden id="performAction" path="action"/>
        </form:form>
      </div>        

      <div id="sidebar" class="grid_3 omega">
         <p>
           Chirper is an example project, created to learn and demonstrate how to use
           Non-relational databases.  Running the project will present a website that has similar
           functionality to Twitter/Twissandra.
        </p>                
      </div>
    </div>


  </body>
</html>