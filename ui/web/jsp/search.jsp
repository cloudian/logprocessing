<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
 
	<link rel="stylesheet" href="css/style.css" type="text/css" media="all" />
  
    <script language="javascript" type="text/javascript" src="js/datetimepicker.js">
      //Date Time Picker script- by TengYong Ng of http://www.rainforestnet.com
	  //Script featured on JavaScript Kit (http://www.javascriptkit.com)
	  //For this script, visit http://www.javascriptkit.com
    </script>

    <title>CDR search</title>
  </head>
  
  <script type="text/javascript">  
    function submitAction(actionType)
    {    
      var action = document.getElementById('performAction');
  	  action.value = actionType;

  	  var form = document.getElementById('searchfrm');

  	  form.submit();
  	  return true;    	
    } 

    function downloadCSV()
    {
		var msisdn = document.getElementById('id_msisdn');
		var fromDate = document.getElementById('id_start_time');
		var toDate = document.getElementById('id_end_time');
		var market = document.getElementById('id_market');
		var type = document.getElementById('id_type');
		
		//alert("msisdn: " + msisdn.value + ",starttime: " + fromDate.value + ", toDate: " + toDate.value +
		//	  ",market: " + market.value + ",type: " + type.value);

		var url = "downloadCSV.htm?msisdn="+msisdn.value+"&market="+market.value+"&type="+type.value+
				  "&starttime="+escape(fromDate.value)+"&endtime="+
				  escape(toDate.value);

		alert("url: " + url);
        newUserWin = window.open(url);
		return true;		
   }
    
  </script>
  
  <body>
<!-- Header -->
<div id="header">
	<div class="shell">
		<!-- Logo + Top Nav -->
		<div id="top">
			<h1><a href="#">CDR Search</a></h1>
			<!-- 
			<div id="top-navigation">
				Welcome <a href="#"><strong>Administrator</strong></a>
				<span>|</span>
				<a href="#">Help</a>
				<span>|</span>
				<a href="#">Log out</a>
			</div>
			-->
		</div>
		<!-- End Logo + Top Nav -->
		
		<!-- Main Nav 
		<div id="navigation">
			<ul>
			    <li><a href="#" class="active"><span>Dashboard</span></a></li>
			    <li><a href="#"><span>New Articles</span></a></li>
			    <li><a href="#"><span>User Management</span></a></li>
			    <li><a href="#"><span>Photo Gallery</span></a></li>
			    <li><a href="#"><span>Products</span></a></li>
			    <li><a href="#"><span>Services Control</span></a></li>
			</ul>
		</div>
		 End Main Nav -->
	</div>
</div>
<!-- End Header -->
<!-- Container -->
<div id="container">
	<div class="shell">
		
		<!-- Small Nav 
		<div class="small-nav">
			<a href="#">Dashboard</a>
			<span>&gt;</span>
			Current Articles
		</div>
		 End Small Nav -->
		
        <spring:hasBindErrors name="searchCmd">
		  <div class="msg msg-error">
            <c:forEach var="errMsgObj" items="${errors.allErrors}">
			  <p><strong><spring:message message="${errMsgObj}"/></strong></p>
            </c:forEach>
		  </div>
        </spring:hasBindErrors> 		
				 
		<br />
		<!-- Main -->
		<div id="main">
			<div class="cl">&nbsp;</div>
			
     		<form:form id="searchfrm" commandName="searchCmd" >
			<!-- Content -->
			<div id="content">
				
				<!-- Box -->
				<div class="box">
					<!-- Box Head -->
					<div class="box-head">
						<h2>Search CDR Records</h2>
					</div>
					<!-- End Box Head -->
											
						<!-- Form -->
						<div class="form">						
						      <table width="80%" border="0" cellspacing="0" cellpadding="0">
							    <tr>
								  <td>
								    <p class="inline-field">
									  <label>MSISDN</label>
									  <form:input id="id_msisdn" path="msisdn"  cssClass="field size3" size="15" />									
								    </p>	
								  </td>
								  <td>
								    <p class="inline-field">
									  <label>Market</label>
							  		  <form:select id="id_market" path="market" cssClass="field" cssStyle="width: 90px;">
								 		  <form:options items="${marketTypes}" itemValue="code" itemLabel="label" />  
									  </form:select>
								    </p>	
								  </td>							
							    </tr>
							    <tr>
								  <td>
								    <p class="inline-field">
									  <label>From Date/Time</label>
									  <form:input id="id_start_time" cssClass="field size3" path="fromDate" size="25"/><a href="javascript:NewCal('id_start_time','mmmddyyyy',true,12)"><img src="images/cal.gif" width="16" height="16" border="0" alt="Pick a date"/></a>         
								    </p>
								  </td>	
								  <td>
								    <p class="inline-field">
									  <label>Message Type</label>
									  <form:select id="id_type" path="messageType" cssClass="field" cssStyle="width: 90px;">
								 		  <form:options items="${msgTypes}" itemValue="code" itemLabel="label" />  
									  </form:select>
								    </p>	
								
								  </td>							
							    </tr>
							    <tr>
								  <td>
								    <p class="inline-field">
									  <label>To Date/Time</label>
									  <form:input id="id_end_time" cssClass="field size3" path="toDate" size="25"/><a href="javascript:NewCal('id_end_time','mmmddyyyy',true,12)"><img src="images/cal.gif" width="16" height="16" border="0" alt="Pick a date"/></a>         
								    </p>
								  </td>	
							    </tr>
							
						      </table>
								
						</div>
						<!-- End Form -->
						
						<!-- Form Buttons -->
						<div class="buttons">
							<input type="button" class="button" value="Search" onclick="submitAction('search');"/>
							<input type="button" class="button" value="Graph" onclick="submitAction('graph');"/>							
							<input type="button" class="button" value="Download CSV" onclick="submitAction('downloadcsv');"/>
						</div>
						<!-- End Form Buttons -->
						
					  <form:hidden id="performAction" path="action"/>
						
					
				</div>
				<!-- End Box -->

				<!-- Box -->
				<div class="box">
					<!-- Box Head -->
					<div class="box-head">
						<h2 class="left">Search Results</h2>
					</div>
					<!-- End Box Head -->	

					      <c:choose>
 		                    <c:when test="${searchCmd.graph}">
		                      <c:choose>
		                        <c:when test="${fn:length(sessionScope.chartData) < 1}">
				                  <p></p>
					              <p><b>No data found.</b></p>
				                </c:when>
                                <c:otherwise>
                                  <img align="top" src="cdrgraph.png?${searchCmd.graphQueryString}">  
                                </c:otherwise>
		                      </c:choose>
                            </c:when>
                            <c:otherwise>  

					<!-- Table -->
					<div class="table">
						<table width="100%" border="0" cellspacing="0" cellpadding="0">
							<tr>
								<th>Date/Time</th>
								<th>Market</th>
								<th>Type</th>
								<th>MSISDN</th>
								<th>MO IP addr</th>
								<th>MT IP addr</th>
								<th>Sender domain</th>
								<th>Recipient domain</th>
							</tr>
							
							<c:forEach var="cdrItem" items="${cdrs}" varStatus="cdrStatus">
  							  <tr>
								<td><h3><a href="#"><c:out value='${cdrItem.displayTimestamp}'/></a></h3></td>
								<td><c:out value='${cdrItem.market}'/></td>
								<td><c:out value='${cdrItem.type}'/></td>
								<td><c:out value='${cdrItem.msisdn}'/></td>
								<td><c:out value='${cdrItem.moIPAddress}'/></td>
								<td><c:out value='${cdrItem.mtIPAddress}'/></td>
								<td><c:out value='${cdrItem.senderDomain}'/></td>
								<td><c:out value='${cdrItem.recipientDomain}'/></td>
							  </tr>
            				</c:forEach>
														
						</table>
						<div class="buttons">
				          <input type="button" value="More" class="button" onclick="submitAction('getmore');"/>
						</div>
						
						
						<!-- Pagging
						<div class="pagging">
							<div class="left">Showing 1-12 of 44</div>
							<div class="right">
								<a href="#">Previous</a>
								<a href="#">1</a>
								<a href="#">2</a>
								<a href="#">3</a>
								<a href="#">4</a>
								<a href="#">245</a>
								<span>...</span>
								<a href="#">Next</a>
								<a href="#">View all</a>
							</div>
						</div>
						 End Pagging -->
						
					</div>
					<!-- Table -->
				    </c:otherwise>
 	              </c:choose>
					
				</div>
				<!-- End Box -->
				

			</div>
			<!-- End Content -->
			</form:form>
			
			
			<div class="cl">&nbsp;</div>			
		</div>
		<!-- Main -->
	</div>
</div>
<!-- End Container -->

<!-- Footer -->
<div id="footer">
	<div class="shell">
		<span class="left">&copy; 2010 - Gemini Mobile Technologies</span>
		<span class="right">
		</span>
	</div>
</div>
<!-- End Footer -->


  </body>
</html>