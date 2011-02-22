//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package com.geminimobile.web;

import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import com.geminimobile.chart.ChartSeries;
import com.geminimobile.chart.GeminiChartUtil;

public class GeminiChartController extends AbstractController 
{
	//This controller linked to by an img control on gemstats.jsp (message stats reporting page).
	//Call the JFreeChart tool to generate the chart. The result goes into the OutputStream.
	//Only cummulative-type reports use this controller - not snapshot-type.
		  
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception 
    {    	
    	// The data to be reported on this chart was put in the session by GenerateGemStatsController
    	// Example of query string is: 
        //    tableTitleText=MM3 Messages Sent by Hour for Service Provider 1111 for Vasp 4444&intervalText=Hour&messageType=MM3.MPS;
    	List<ChartSeries> chartData = (List<ChartSeries>) request.getSession(true).getAttribute("chartData");		
    	String chartTitle = (String) request.getParameter("chartTitle");
    	String intervalText = (String) request.getParameter("intervalText");    	
                      		
        try
        {	
        	response.setContentType("image/png");        
        	if (chartData != null && chartData.size() > 0){

        		//Create the chart with chart tool
        		GeminiChartUtil chartUtil = new GeminiChartUtil();

        		// chartData - are the data points themselves- ex. [<number>, <time interval>]
        		// tableTitleText - is displayed as the chart's title. Ex. "MM3 Msgs Sent for Service Provider 1111 and Vasp 6543"
        		// intervalText - displayed as part of the X-Axis label. Ex. "Hour"
        		chartUtil.createReport(response.getOutputStream(), chartData, chartTitle, intervalText);     
        	} 	
        	
        	// write data to CSV file
    //        System.out.println("GCC Calling CSV generation");
    //        GemStatsCSVGeneration csvGeneration = new GemStatsCSVGeneration();
    //        String csvData = csvGeneration.generateCSV(chartData, messageType);
            
    //        request.getSession().setAttribute(CSVDATA,csvData);  // set in session for csv file data
           

        }
        catch(Exception e)
        {
        	e.printStackTrace();
        	throw new ServletException("Could not get statistic values.  Probably a Database issue");
        }
        return null;
    }

}
