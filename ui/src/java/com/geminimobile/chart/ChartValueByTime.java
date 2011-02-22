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

import java.text.SimpleDateFormat;
import java.util.Date;

// Represents a single category and value for display on a bar chart.
// The category is a timestamp.
public class ChartValueByTime implements IChartValue {

	private int m_statValue;
	private long m_timestamp;
	private String m_statType = "";
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("MMMM-dd hh:mm a");
	
	public ChartValueByTime(int val, long timestamp)
	{
		m_statValue = val;
		m_timestamp = timestamp;
	}
	
	public  ChartValueByTime(int val, long timestamp , String statType)
	{
		m_statValue = val;
		m_timestamp = timestamp;
		m_statType = statType;
	}
	
	public String getCategory() 
	{
		String time = sdf.format(new Date(m_timestamp));

		return time;
	}

	public int getValue() 
	{
		return m_statValue;
	}
	
	public String getStatType() 
	{
		return m_statType;
	}
	
	public long getTime()
	{
		return m_timestamp;
	}
}
