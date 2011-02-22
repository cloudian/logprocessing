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

import java.util.Date;
import java.util.Vector;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import com.geminimobile.CDRDataAccess;
import com.geminimobile.CDRDataAccess.CDREntry;


/**
 * Spring controller to process CSV downloads.
 * Data is stored in the session. This controller will get it, and convert to CSV.
 * 
 * @author snaik
 */
public class DownloadCSVController extends AbstractController {
    
    public DownloadCSVController() {}
    
        
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception 
	{	
		String msisdn = (String) request.getParameter("msisdn");
		String startTime = (String) request.getParameter("starttime");
		String endTime = (String) request.getParameter("endtime");
		String market = (String) request.getParameter("market");
		String type = (String) request.getParameter("type");
		
        Date toDate = SearchCommand.m_sdf.parse(endTime);        
        //String maxTimestamp = Long.toString(toDate.getTime());
        
        Date fromDate = SearchCommand.m_sdf.parse(startTime);
        //String minTimestamp = Long.toString(fromDate.getTime());
        
        CDRDataAccess cdrAccess = new CDRDataAccess();
        Vector<CDREntry> cdrs = cdrAccess.getCDRsByMSISDN(msisdn, fromDate.getTime(), toDate.getTime(), market,
        												  type, 100000); // 100,000 entries max

		//Vector<CDREntry> cdrs = (Vector<CDREntry>) request.getSession().getAttribute("cdrResults");

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
	} 

    
            
}


