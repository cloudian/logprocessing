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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import com.geminimobile.CDRDataAccess;
import com.geminimobile.CDRDataAccess.CDREntry;
import com.geminimobile.chart.ChartSeries;


/**
 * Form controller to process search form submission.
 * 
 * @author snaik
 */
public class SearchController extends SimpleFormController {

	public static int MAX_ENTRIES_PER_PAGE = 20;
    public SearchController() {}
    
    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, 
			Object command, BindException arg3) throws Exception    
    {
    	SearchCommand cmd = (SearchCommand) command;
        ModelAndView mav = new ModelAndView(getSuccessView());
        
        CDRDataAccess cdrAccess = new CDRDataAccess();
                
        Date toDate = SearchCommand.m_sdf.parse(cmd.getToDate());        
        long maxTimestamp = toDate.getTime();
                
        Date fromDate = SearchCommand.m_sdf.parse(cmd.getFromDate());
        long minTimestamp = fromDate.getTime();
        
        String msisdn = cmd.getMsisdn();
        String action = cmd.getAction();
		Vector<CDREntry> cdrs;
		
        if (action.compareToIgnoreCase("downloadcsv")==0) {

        	if (msisdn != null && msisdn.length() > 0) {
        		cdrs = cdrAccess.getCDRsByMSISDN(cmd.getMsisdn(), minTimestamp, maxTimestamp, cmd.getMarket(), 
            												  cmd.getMessageType(), 100000); // 100,000 entries max
        	} 
        	else {
        		cdrs = cdrAccess.getCDRsByHour(minTimestamp, maxTimestamp, cmd.getMarket(), cmd.getMessageType(), MAX_ENTRIES_PER_PAGE);
        	}
    		StringBuffer sb = new StringBuffer();
    		
    		// Write column headers 
    		sb.append("Date/Time,Market,Type,MSISDN,MO IP address,MT IP Address,Sender Domain,Recipient Domain\n");
    				
    		for (CDREntry entry: cdrs) {			
    			sb.append(entry.getDisplayTimestamp() + "," + entry.getMarket() + "," + entry.getType() + "," + entry.getMsisdn() + "," +
    					  entry.getMoIPAddress() + "," + entry.getMtIPAddress() + "," + entry.getSenderDomain() + "," +
    					  entry.getRecipientDomain() + "\n");
    		}
    		
    		String csvString = sb.toString();
    		
    		response.setBufferSize(sb.length());
    		response.setContentLength(sb.length());
    		response.setContentType( "text/plain; charset=UTF-8" );
    		//response.setContentType( "text/csv" );
    		//response.setContentType("application/ms-excel");
    		//response.setHeader("Content-disposition", "attachment;filename=cdrResults.csv");
    		response.setHeader("Content-Disposition", "attachment; filename=" + "cdrResults.csv" + ";");
    		ServletOutputStream os = response.getOutputStream();

    		os.write( csvString.getBytes() );

    		os.flush();
    		os.close();
    		
            return null;
        	
        } else if (action.compareToIgnoreCase("graph")==0) {
        	cmd.setGraph(true);
        	List<ChartSeries> chartData;
        	if (msisdn != null && msisdn.length() > 0) {
            	chartData = cdrAccess.getChartDataByMSISDN(cmd.getMsisdn(), minTimestamp, maxTimestamp, 
						 cmd.getMarket(), cmd.getMessageType(), 100000);
        	} 
        	else {
        		chartData = cdrAccess.getChartDataByHour(minTimestamp, maxTimestamp, cmd.getMarket(), cmd.getMessageType(), 100000);
        	}

        	request.getSession().setAttribute("chartData", chartData);
        	
        }  else if (action.compareToIgnoreCase("getmore") ==0) {    		
    		cdrs = (Vector<CDREntry>) request.getSession().getAttribute("currentcdrs");
    		int numCDRs = cdrs.size();
    		CDREntry lastCDR = cdrs.get(numCDRs - 1);
    		long lastCDRTime = Long.parseLong(lastCDR.getTimestamp());

    		if (msisdn != null && msisdn.length() > 0) {
    			Vector<CDREntry> moreCDRs  = cdrAccess.getCDRsByMSISDN(cmd.getMsisdn(), lastCDRTime, maxTimestamp,cmd.getMarket(), 
            									                       cmd.getMessageType(), MAX_ENTRIES_PER_PAGE); 
        		cdrs.addAll(moreCDRs);    		
        	} 
        	else {
        		Vector<CDREntry> moreCDRs = cdrAccess.getCDRsByHour(lastCDRTime, maxTimestamp, cmd.getMarket(), cmd.getMessageType(), MAX_ENTRIES_PER_PAGE);
        		cdrs.addAll(moreCDRs);    		
        	}

    		request.getSession().setAttribute("currentcdrs", cdrs);
    		mav.addObject("cdrs", cdrs);
    	}
        else {
        	// Normal search            
        	if (msisdn != null && msisdn.length() > 0) {
        		cdrs = cdrAccess.getCDRsByMSISDN(cmd.getMsisdn(), minTimestamp, maxTimestamp,cmd.getMarket(), 
            												  cmd.getMessageType(), MAX_ENTRIES_PER_PAGE); 
        	} 
        	else {
        		cdrs = cdrAccess.getCDRsByHour(minTimestamp, maxTimestamp, cmd.getMarket(), cmd.getMessageType(), MAX_ENTRIES_PER_PAGE);
        	}

            request.getSession().setAttribute("currentcdrs", cdrs);
    		mav.addObject("cdrs", cdrs);
        }       
		
        mav.addObject("searchCmd", cmd);

        List<Option> msgOptions = getMessageOptions();
        mav.addObject("msgTypes", msgOptions);
        List<Option> marketOptions = getMarketOptions();
        mav.addObject("marketTypes", marketOptions);
        
        return mav;
    }
    
        
    @SuppressWarnings("unchecked")
	@Override
    protected Map referenceData(HttpServletRequest req, Object command, Errors errors) throws Exception {
        Map<String, Object> data = new HashMap<String, Object>();

        List<Option> msgOptions = getMessageOptions();
        data.put("msgTypes", msgOptions);
        List<Option> marketOptions = getMarketOptions();
        data.put("marketTypes", marketOptions);
        return data;
    }
    
    protected List<Option> getMessageOptions() {
    	ArrayList<Option> options = new ArrayList<Option>();
    	
    	options.add(new Option("-all-", "-all-"));
    	options.add(new Option("O1S", "O1S"));
    	options.add(new Option("R1Rt", "R1Rt"));    	
    	return options;
    }

    protected List<Option> getMarketOptions() {
    	ArrayList<Option> options = new ArrayList<Option>();
    	
    	options.add(new Option("region1", "region1"));
    	options.add(new Option("region2", "region2"));    	
    	options.add(new Option("region3", "region3"));    	
    	options.add(new Option("region4", "region4"));    	
    	return options;
    }
    
}
