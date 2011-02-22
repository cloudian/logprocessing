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

package com.geminimobile.chart;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
//import org.jfree.ui.TextAnchor;


public class GeminiChartUtil {	
	Random random = new Random();
	
	public void createReport(OutputStream out, List<ChartSeries> chartData, String strTitle, 
    					String strDomainAxisLabel) 
    {        

    	strDomainAxisLabel = "Time Interval: " + strDomainAxisLabel;
        String strYAxisLabel;
        //String strCategoryType = " ";
        strYAxisLabel = "Count";    	
       	//SimpleDateFormat sdf = new SimpleDateFormat("MMMM-dd hh:mm a");

       	//build a dataset as needed by JFreeChart
		//		note: strCategoryType is for a chart with multiple categories.
		//		i.e. idisplay multiple bars for each time interval.
       	//
       	
       	TimeSeriesCollection dataSet = new TimeSeriesCollection();
       	
       	// For each series of data, create a TimeSeries
       	for (int i=0; i < chartData.size(); i++) {
       		ChartSeries chartSeries = chartData.get(i);
       		
       		TimeSeries timeSeries = new TimeSeries(chartSeries.getName(), Millisecond.class);
       		List<ChartValueByTime> data = chartSeries.getData();
       		
       		//int cumulValue = 0;
       		for (int j=0; j < data.size(); j++) {
       			ChartValueByTime chartVal = data.get(j);
       			
       			Millisecond ms = new Millisecond(new Date(chartVal.getTime()));
       			
      			// *NOT* Store the cumulative value . So maintain a running total.       			
       			//cumulValue += chartVal.getValue();
       			//timeSeries.add(ms, cumulValue);
       			timeSeries.add(ms, chartVal.getValue());
       		}
       		dataSet.addSeries(timeSeries);
       	}
        
       	JFreeChart lineChart = ChartFactory.createTimeSeriesChart(
        		strTitle,  					// chart title
        		strDomainAxisLabel,         // domain axis label
                strYAxisLabel,            	// range axis label
                dataSet,                    // data
                true,              			// legend
                false,                      // tooltips
                false                       // urls
            );


        try {
        	ChartUtilities.writeChartAsPNG(out, lineChart, 800, 400);
        }
        catch (IOException e)
        {
        	e.printStackTrace();
        }
        
    }
}
