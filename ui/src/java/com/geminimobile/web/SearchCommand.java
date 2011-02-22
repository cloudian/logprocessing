/*
  Copyright 2011 Gemini Mobile Technologies (http://www.geminimobile.com)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.geminimobile.web;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Spring command object for the CDR Search page. 
 * @author snaik
 */
public class SearchCommand {
	private String action;
	private String msisdn; 
	private String market;
	private String messageType;
	private String fromDate;
	private String toDate;
	private boolean graph;
	private String graphQueryString;
	

	public static final String TimeStampFormat = "MMM-dd-yyyy hh:mm a";
	public static SimpleDateFormat m_sdf = new SimpleDateFormat(TimeStampFormat);	
	
	public SearchCommand() {
    	// Set default start and end time.
    	Calendar now = Calendar.getInstance();
    			
		toDate = m_sdf.format(new Date(now.getTimeInMillis()));		
		long yesterdayMillis = now.getTimeInMillis() - (1000L * 60L * 60L * 24L) ; // 1 day earlier
		fromDate = m_sdf.format(new Date(yesterdayMillis));
	}    
	
	public boolean isGraph() {
		return graph;
	}
	public void setGraph(boolean isGraph) {
		this.graph = isGraph;
	}
	public String getGraphQueryString() {
		return graphQueryString;
	}
	public void setGraphQueryString(String graphQueryString) {
		this.graphQueryString = graphQueryString;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public String getMarket() {
		return market;
	}

	public void setMarket(String market) {
		this.market = market;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public String getFromDate() {
		return fromDate;
	}

	public void setFromDate(String fromDate) {
		this.fromDate = fromDate;
	}

	public String getToDate() {
		return toDate;
	}

	public void setToDate(String toDate) {
		this.toDate = toDate;
	}

}
