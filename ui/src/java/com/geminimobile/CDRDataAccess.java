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

package com.geminimobile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.Vector;


import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.Row;

import org.apache.log4j.Logger;

import com.geminimobile.chart.ChartSeries;
import com.geminimobile.chart.ChartValueByTime;
import com.geminimobile.util.Configuration;

public class CDRDataAccess {
	
	private static final Logger logger = Logger.getLogger(CDRDataAccess.class);
	public static final String TimeStampFormat = "HH:mm MMM-dd-yyyy ";
	public static SimpleDateFormat m_sdf = new SimpleDateFormat(TimeStampFormat);

	public static final String HourlyTimeStampFormat = "yyyyMMddHH"; 
    public static SimpleDateFormat m_sdfHourly = new SimpleDateFormat(HourlyTimeStampFormat); 


	public static final String KS_NAME = "CDRLogs";
	public static final String CF_CDRENTRY = "CDREntry";	
	public static final String CF_MSISDNTIMELINE = "MSISDNTimeline";
	public static final String CF_HOURLYTIMELINE = "HourlyTimeline";		
	
	public static final String COL_ID = "id";
	public static final String COL_MSISDN = "msisdn";
	public static final String COL_TIMESTAMP = "timestamp";
	public static final String COL_MOIPADDRESS = "moipaddress";
	public static final String COL_MTIPADDRESS = "mtipaddress";
	public static final String COL_SENDERDOMAIN = "senderdomain";
	public static final String COL_RECIPIENTDOMAIN = "recipientdomain";
	public static final String COL_TYPE = "type";
	public static final String COL_MARKET = "market";
	public static final long MS_PER_HOUR = 3600000L;
	public static final String MESSAGE_TYPE_ALL = "-all-";
	
	public static final int LIMIT = 10000; // Number of values to get in a chunk
	
	public class CDREntry {
		String entryId;
		String msisdn;
		String type;
		String moIPAddress;
		String mtIPAddress;
		String senderDomain;
		String recipientDomain;
		String timeStamp;
		String displayTimestamp;
		String market;
		
		public CDREntry(String entryId, String msisdn, String type, String moIPAddress, String mtIPAddress, 
						String senderDomain, String recipientDomain, String timeStamp, String market) {
			this.entryId = entryId;
			this.msisdn = msisdn;
			this.type = type;
			this.moIPAddress = moIPAddress;
			this.mtIPAddress = mtIPAddress;
			this.senderDomain = senderDomain;
			this.recipientDomain = recipientDomain;
			this.setTimestamp(timeStamp);
			this.market = market;
		}
		
		public String getMarket() {
			return market;
		}
		public void setMarket(String market) {
			this.market = market;
		}
		public String getEntryId() {
			return entryId;
		}
		public void setEntryId(String entryId) {
			this.entryId = entryId;
		}
		public String getMsisdn() {
			return msisdn;
		}
		public void setMsisdn(String msisdn) {
			this.msisdn = msisdn;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String getMoIPAddress() {
			return moIPAddress;
		}
		public void setMoIPAddress(String moIPAddress) {
			this.moIPAddress = moIPAddress;
		}
		public String getMtIPAddress() {
			return mtIPAddress;
		}
		public void setMtIPAddress(String mtIPAddress) {
			this.mtIPAddress = mtIPAddress;
		}
		public String getSenderDomain() {
			return senderDomain;
		}
		public void setSenderDomain(String senderDomain) {
			this.senderDomain = senderDomain;
		}
		public String getRecipientDomain() {
			return recipientDomain;
		}
		public void setRecipientDomain(String recipientDomain) {
			this.recipientDomain = recipientDomain;
		}
		public String getTimestamp() {
			return timeStamp;
		}
		public void setTimestamp(String timestamp) {
			this.timeStamp = timestamp;
			
			// Convert timestamp for display
			long nTime = Long.parseLong(timestamp);
			
			String timeStr = m_sdf.format(new Date(nTime));				

			this.setDisplayTimestamp(timeStr);			
		}
		public String getDisplayTimestamp() {
			return displayTimestamp;
		}
		public void setDisplayTimestamp(String displayTimestamp) {
			this.displayTimestamp = displayTimestamp;
		}
		
	}
	
	
	// CDREntry: CDR Log data
	//    * key: UUID 
	// 	  * column: "id"	    	value: cdrID unique UUID
	//	  * column: "type"      	value: type of CDR (MO or MT)
	//	  * column: "moIPAddress" 	value: IP address of MO
	//	  * column: "mtIPAddress"   value: IP address of MT
	//	  * column: "senderDomain"  value: Sender Domain
	//	  * column: "recipientDomain" value: Recipient domain
	//    * column: "timestamp" 	value: Date/Time for CDR Entry
	private DataAccessObject m_daoCDREntry;

	// TIMELINE: A particular MSISDN's timeline (CDR entries)
	//    * key: msisdn
	//    * column_name: timestamp value.  column_value: cdrID
	private DataAccessObject m_daoMSISDNTimeline;
	
	// TIMELINE: CDR entries grouped by the hour.
	//    * key: timestamp (truncated to the hour - long converted to string)
	//    * column_name: timestamp value.  column_value: cdrID
	private DataAccessObject m_daoHourlyTimeline;
	
	
	public CDRDataAccess() {
		final String sKeyspace = KS_NAME;
		final String sHost = Configuration.get("host", "localhost");
		String strPort = Configuration.get("port", "9160");
		final int nPort = Integer.parseInt(strPort);
		this.m_daoCDREntry = new DataAccessObject(sKeyspace, sHost, nPort, CF_CDRENTRY);
		this.m_daoMSISDNTimeline = new DataAccessObject(sKeyspace, sHost, nPort, CF_MSISDNTIMELINE);
		this.m_daoHourlyTimeline = new DataAccessObject(sKeyspace, sHost, nPort, CF_HOURLYTIMELINE);
	}
	
	
	public void deleteAll() throws Exception {
		m_daoCDREntry.deleteAll();
		m_daoMSISDNTimeline.deleteAll();
		m_daoHourlyTimeline.deleteAll();
	}

	// Delete all entries before (and including) a particular hour timestamp.  
	// For the hourly timeline, 
	public void deleteBeforeTS(long hourStamp) {
		String sLastHour = m_sdfHourly.format(new Date(hourStamp));
				
		String strLastHourCompare = String.format("%013d", hourStamp);

		// Need to clean up MSISDNTimeline, HourlyTimeline, and CDREntry CFs.  For HourlyTimeline, get all rows before the hourStamp,
		// and delete the corresponding records in the CDREntry table. Then delete the rows from the HourlyTimeline.  From
		// the MSISDNTimeline, we need to scan all rows, and grab all columns (timestamps) less than the hourStamp, and delete
		// those columns.
		try {
			boolean bMoreRows = true;
			
			String begRangeKey = "";
			// Handle HourlyTimeline and CDREntry CFs first
			while (bMoreRows) {
				List<Row<String, String, String>> rows = m_daoHourlyTimeline.getRangeSlice(begRangeKey, sLastHour, "", LIMIT, false);				
				
				if (rows.size() > 0) {			
					Row<String, String, String> lastRow = rows.get(rows.size() - 1);
					begRangeKey = lastRow.getKey(); // Get start key for next range slice
					for (Row<String, String, String> row: rows) {
						List<HColumn<String, String>> cols = row.getColumnSlice().getColumns();						
						
						for (HColumn<String, String> col: cols) {
							String entryId = col.getValue();
							m_daoCDREntry.delete(entryId, null, StringSerializer.get()); // Delete individual CDR entry
						}
						String hourKey = row.getKey();
						m_daoHourlyTimeline.delete(hourKey, null, StringSerializer.get()); // Delete entire row.
					}
					
					if (rows.size() == 1) {
						bMoreRows = false; // this is the last row in the slice - data was already deleted.
					}

				}
				else {
					bMoreRows = false; // Done operating on rows for HourlyTimeline
				}
			}
			
			// Now take care of MSISDNTimeline
			bMoreRows = true;
			begRangeKey = "";
			while (bMoreRows) {
				List<Row<String, String, String>> rows = m_daoMSISDNTimeline.getRangeSlice(begRangeKey, "", "", LIMIT, false);
				if (rows.size() > 0) {
					Row<String, String, String> lastRow = rows.get(rows.size() - 1);
					begRangeKey = lastRow.getKey(); // Get start key for next range slice
				
					for (Row<String, String, String> row: rows) {
						List<HColumn<String, String>> cols = row.getColumnSlice().getColumns();
						
						for (HColumn<String, String> col: cols) {
							String colStamp = col.getName();
						
							if (colStamp.compareTo(strLastHourCompare) < 0) { // Lexicographic comparison - want to ignore the UUID portion of column name
								m_daoHourlyTimeline.delete(row.getKey(), colStamp, StringSerializer.get()); // Delete MSISDNTimeline column
								// TODO: Should we optimize to add all deletions for the row, and then execute??
							}
						}

					}
					if (rows.size() == 1) {
						bMoreRows = false; // this is the last row in the slice - data was already deleted.
					}

				}
				else {
					bMoreRows = false; // Done operating on rows for MSISDNTimeline
				}

			}			
		} catch(Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
	}
	
	public Vector<CDREntry> getCDRsForHour(String hour) {
		Vector<CDREntry> vCDRs = new Vector<CDREntry>();

		try {
			List<HColumn<String, String>> cols = m_daoHourlyTimeline.getSlice(hour, "", "", LIMIT);

			for (HColumn<String, String> col : cols) {
				String sEntryID = new String(col.getValue());
				String sType = m_daoCDREntry.get(sEntryID, COL_TYPE, StringSerializer.get());
				String sMarket = m_daoCDREntry.get(sEntryID, COL_MARKET, StringSerializer.get());

				String sMOIPAddress = m_daoCDREntry.get(sEntryID,
							COL_MOIPADDRESS, StringSerializer.get());
				String sMTIPAddress = m_daoCDREntry.get(sEntryID,
						COL_MTIPADDRESS, StringSerializer.get());
				String sSenderDomain = m_daoCDREntry.get(sEntryID,
							COL_SENDERDOMAIN, StringSerializer.get());
				String sRecipientDomain = m_daoCDREntry.get(sEntryID,
							COL_RECIPIENTDOMAIN, StringSerializer.get());
				String sMSISDN = m_daoCDREntry.get(sEntryID, COL_MSISDN, StringSerializer.get());

				String sTimestamp = m_daoCDREntry.get(sEntryID,
							COL_TIMESTAMP, StringSerializer.get());

				CDREntry entry = new CDREntry(sEntryID, sMSISDN, sType,
							sMOIPAddress, sMTIPAddress, sSenderDomain,
							sRecipientDomain, sTimestamp, sMarket);
				vCDRs.add(entry);
			}

			
		} catch(Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		
		return vCDRs;
	}

	// Query the DB to get a list of counts and an hourly timestamp. This is for all msisdns for 
	// a particular market and message type.
	public Vector<CDREntry> getCDRsByHour(long minTimestamp, long maxTimestamp, 
										  String market, String messageType, int limit) {
		Vector<CDREntry> vCDRs = new Vector<CDREntry>();
		
		// Truncate minTime and maxTime to nearest hour to get row keys.
		long minHour = ( minTimestamp / MS_PER_HOUR ) * MS_PER_HOUR;
		long maxHour = ( maxTimestamp / MS_PER_HOUR ) * MS_PER_HOUR;
		
		String strMinHour = m_sdfHourly.format(new Date(minHour));
		String strMaxHour = m_sdfHourly.format(new Date(maxHour));
		
		int numRecordsRetrieved = 0;
		String strBegColTS = "";
		try {	
			// Get a chunk of rows (hourly data). TODO: Need to take care of the last row in the 
			// list as a special case - since it will probably not contain all the entries for the hour.
			// TODO: Query the DB in a loop, getting up to 'limit' records between min and max timestamp.
			int ROWLIMIT = 5; // Get at most 5 hours of data at a time.
			while (numRecordsRetrieved < limit) {
			
				List<Row<String, String, String>> rows = m_daoHourlyTimeline.getRangeSlice(strMinHour, strMaxHour, 
																					"", ROWLIMIT, true);
				
				for (Row<String, String, String> row : rows) {
					// One row contains all the CDRs for a particular hour.  				
					List<HColumn<String,String>> cols = row.getColumnSlice().getColumns();
				
					for (int i = 0;  i<cols.size();  ++i) {
						HColumn<String,String> result = cols.get(i);						
						String sEntryID = new String(result.getValue());
						String sType = m_daoCDREntry.get(sEntryID, COL_TYPE, StringSerializer.get());
						String sMarket = m_daoCDREntry.get(sEntryID, COL_MARKET, StringSerializer.get());
						
						String entryTimestampStr = result.getName();
						String[] colNameParts = entryTimestampStr.split("_");
						if (colNameParts == null || colNameParts.length < 2) {
							logger.error("Invalid key from MSISDNTimeline table: " + entryTimestampStr);
							continue;
						}
						long entryTimestamp = Long.parseLong(colNameParts[0]);
						
						// filter out any entries not within the time stamp range.
						if (entryTimestamp > maxTimestamp || entryTimestamp < minTimestamp) {
							continue;
						}

						// Filter by market and message type
						if ( sType == null || sMarket == null || (sType.compareTo(messageType) != 0 && !messageType.equals(MESSAGE_TYPE_ALL)) || sMarket.compareTo(market) != 0)  {
							continue; // Skip this entry
						}
					
						String sMOIPAddress = m_daoCDREntry.get(sEntryID, COL_MOIPADDRESS, StringSerializer.get());
						String sMTIPAddress = m_daoCDREntry.get(sEntryID, COL_MTIPADDRESS, StringSerializer.get());
						String sSenderDomain = m_daoCDREntry.get(sEntryID, COL_SENDERDOMAIN, StringSerializer.get());
						String sRecipientDomain = m_daoCDREntry.get(sEntryID, COL_RECIPIENTDOMAIN, StringSerializer.get());
						String sMSISDN = m_daoCDREntry.get(sEntryID, COL_MSISDN, StringSerializer.get());
						String sTimestamp = m_daoCDREntry.get(sEntryID, COL_TIMESTAMP, StringSerializer.get());
					
						CDREntry entry = new CDREntry(sEntryID, sMSISDN, sType, sMOIPAddress, sMTIPAddress, sSenderDomain,
												  sRecipientDomain, sTimestamp, sMarket);
						vCDRs.add(entry);
						numRecordsRetrieved++;
					}			

				}
			}
		} catch(Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		
		return vCDRs;
	}

	public List<ChartSeries> getChartDataByHour(long minTimestamp, long maxTimestamp, String market,
												String messageType, int limit) {
		List<ChartSeries> chartData = new ArrayList<ChartSeries>();
		ChartSeries series = new ChartSeries("CDR count for all MSISDNs for " + market +  ", with type = " + messageType);

		List<ChartValueByTime> chartVals = new ArrayList<ChartValueByTime>();

		try {
			maxTimestamp--;			// Modify the maxTimestamp, since it is 'inclusive'. Decrease it by 1ms
			// Truncate minTime and maxTime to nearest hour
			long maxHour = ( maxTimestamp / MS_PER_HOUR ) * MS_PER_HOUR;
			long minHour = ( minTimestamp / MS_PER_HOUR ) * MS_PER_HOUR;
			
			String strMinHour = m_sdfHourly.format(new Date(minHour));
			String strMaxHour = m_sdfHourly.format(new Date(maxHour));
			//long begColTS = 0;
			int currentCount = 0;

			// Get a chunk of rows - one row contains one data value.  TODO: Need to take care of the last row in the 
			// list as a special case - since it will probably not contain all the entries for the hour.
			// TODO: query the DB in a loop, getting all records between min and max hour.
			List<Row<String, String, String>> rows = m_daoHourlyTimeline.getRangeSlice(strMinHour, strMaxHour, 
																					"", LIMIT, false);

			for (Row<String, String, String> row : rows) {
				String strTime = row.getKey(); // Hourly time stamp.
				long timeStamp = Long.parseLong(strTime);
				
				// One row contains all the CDRs for a particular hour.  Count them.
				List<HColumn<String,String>> cols = row.getColumnSlice().getColumns();				
				for (int i = 0;  i<cols.size();  ++i) {
					HColumn<String,String> result = cols.get(i);					
					String sEntryID = new String(result.getValue());
					String sType = m_daoCDREntry.get(sEntryID, COL_TYPE, StringSerializer.get());
					String sMarket = m_daoCDREntry.get(sEntryID, COL_MARKET, StringSerializer.get());
					// Filter by market and message type
					if ( sType == null || sMarket == null || (sType.compareTo(messageType) != 0 && !messageType.equals(MESSAGE_TYPE_ALL)) || sMarket.compareTo(market) != 0)  {
						continue; // Skip this entry
					}					
					currentCount++;
				}

				if (currentCount > 0) {					
					ChartValueByTime chartval = new ChartValueByTime(currentCount, timeStamp);
					chartVals.add(chartval);					
				}			
				currentCount = 0;

			}


		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		series.setData(chartVals);
		chartData.add(series);
		return chartData;
	}

	/**
	 * 
	 */
	public Vector<CDREntry> getCDRsByMSISDN(String msisdn, long minTimestamp, long maxTimestamp, String market,
											String messageType, int limit) {
		Vector<CDREntry> vCDRs = new Vector<CDREntry>();
		try {	
			// Modify the maxTimestamp, since it is 'inclusive'.  Decrease it by 1 ms			
			maxTimestamp--;
			
			String strMinStamp = Long.toString(minTimestamp);
			String strMaxStamp = Long.toString(maxTimestamp);
			
			List<HColumn<String,String>> results = m_daoMSISDNTimeline.getSliceUsingTimestamp(msisdn, strMinStamp, strMaxStamp, limit, true);

			
			//final int nMaxTweets = java.lang.Math.min(20, results.size());
			for (int i = 0;  i<results.size();  ++i) {
				HColumn<String,String> result = results.get(i);
				String sEntryID = new String(result.getValue());
				String sType = m_daoCDREntry.get(sEntryID, COL_TYPE, StringSerializer.get());
				String sMarket = m_daoCDREntry.get(sEntryID, COL_MARKET, StringSerializer.get());
				// Filter by market and message type
				if ( sType == null || sMarket == null || (sType.compareTo(messageType) != 0 && !messageType.equals(MESSAGE_TYPE_ALL)) || sMarket.compareTo(market) != 0)  {
					continue; // Skip this entry
				}
				
				String sMOIPAddress = m_daoCDREntry.get(sEntryID, COL_MOIPADDRESS, StringSerializer.get());
				String sMTIPAddress = m_daoCDREntry.get(sEntryID, COL_MTIPADDRESS, StringSerializer.get());
				String sSenderDomain = m_daoCDREntry.get(sEntryID, COL_SENDERDOMAIN, StringSerializer.get());
				String sRecipientDomain = m_daoCDREntry.get(sEntryID, COL_RECIPIENTDOMAIN, StringSerializer.get());
				
				String sTimestamp = m_daoCDREntry.get(sEntryID, COL_TIMESTAMP, StringSerializer.get());

				CDREntry entry = new CDREntry(sEntryID, msisdn, sType, sMOIPAddress, sMTIPAddress, sSenderDomain,
											  sRecipientDomain, sTimestamp, sMarket);
				vCDRs.add(entry);
			}			
			
		} catch(Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return vCDRs;
	}
	
	public List<ChartSeries> getChartDataByMSISDN(String msisdn, long minTimestamp, long maxTimestamp,
												  String market, String messageType, int limit) {
		List<ChartSeries> chartData = new ArrayList<ChartSeries>();
		ChartSeries series = new ChartSeries("CDR count for : " + msisdn);
		
		List<ChartValueByTime> chartVals= new ArrayList<ChartValueByTime>();
		
		try {	
			// Modify the maxTimestamp, since it is 'inclusive'.  Decrease it by 1 ms
			maxTimestamp--;
			String strMinStamp = Long.toString(minTimestamp);
			String strMaxStamp = Long.toString(maxTimestamp);

			List<HColumn<String,String>> results = m_daoMSISDNTimeline.getSliceUsingTimestamp(msisdn, strMinStamp, strMaxStamp, limit, false);
			
			long currentHour = minTimestamp;
			currentHour = (currentHour / MS_PER_HOUR) * MS_PER_HOUR;
			int currentCount = 0;
			// Split up the data based on hour
			for (int i=0; i < results.size(); i++) {
				HColumn<String,String> result = results.get(i);
				String entryTimestampStr = result.getName();
				String[] colNameParts = entryTimestampStr.split("_");
				if (colNameParts == null || colNameParts.length < 2) {
					logger.error("Invalid key from MSISDNTimeline table: " + entryTimestampStr);
					break;
				}
				long entryTimestamp = Long.parseLong(colNameParts[0]);
				String sEntryID = new String(result.getValue());
				String sType = m_daoCDREntry.get(sEntryID, COL_TYPE, StringSerializer.get());
				String sMarket = m_daoCDREntry.get(sEntryID, COL_MARKET, StringSerializer.get());				
				//String sTimestamp = m_daoCDREntry.get(sEntryID, COL_TIMESTAMP, StringSerializer.get());

				// Filter by market and message type
				if ( sType == null || sMarket == null || (sType.compareTo(messageType) != 0 && !messageType.equals(MESSAGE_TYPE_ALL)) || sMarket.compareTo(market) != 0)  {
					continue; // Skip this entry
				}

				//long  entryTimestamp = Long.parseLong(sTimestamp);
				if (entryTimestamp < (currentHour + MS_PER_HOUR)) {
					currentCount++; // Keep count of the number of CDRs for this hour
				} 
				else {
					// Done with this hour
					ChartValueByTime chartval = new ChartValueByTime(currentCount, currentHour);
					chartVals.add(chartval);
					
					// Reset counters
					currentHour = currentHour + MS_PER_HOUR;
					currentCount = 1;
				}

			}
			
			// Take care of final hour
			if (currentCount > 0) {
				ChartValueByTime chartval = new ChartValueByTime(currentCount, currentHour);
				chartVals.add(chartval);
				currentCount =0;
			}
			
			
		} catch(Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		series.setData(chartVals);
		chartData.add(series);
		return chartData;
	}

	
	/**
	 *  timeStamp should be in ms
	 */
	public void addCDREntry(String msisdn, String type, String moIPAddress, String mtIPAddress, String senderDomain,
							String recipientDomain, long timeStamp, String market) {
		try {	
			final String sCDREntryUuid = UUID.randomUUID().toString();
			final String sTimestamp = Long.toString(timeStamp);
			m_daoCDREntry.insert(sCDREntryUuid, COL_ID, sCDREntryUuid, StringSerializer.get());
			m_daoCDREntry.insert(sCDREntryUuid, COL_MSISDN, msisdn, StringSerializer.get());
			m_daoCDREntry.insert(sCDREntryUuid, COL_TYPE, type, StringSerializer.get());
			m_daoCDREntry.insert(sCDREntryUuid, COL_MOIPADDRESS, moIPAddress, StringSerializer.get());
			m_daoCDREntry.insert(sCDREntryUuid, COL_MTIPADDRESS, mtIPAddress, StringSerializer.get());
			m_daoCDREntry.insert(sCDREntryUuid, COL_SENDERDOMAIN, senderDomain, StringSerializer.get());
			m_daoCDREntry.insert(sCDREntryUuid, COL_RECIPIENTDOMAIN, recipientDomain, StringSerializer.get());
			m_daoCDREntry.insert(sCDREntryUuid, COL_TIMESTAMP, sTimestamp, StringSerializer.get());
			m_daoCDREntry.insert(sCDREntryUuid, COL_MARKET, market, StringSerializer.get());
			
			final String timeUUID = UUID.randomUUID().toString();
			final String timePlusUUID = String.format("%013d_%s", timeStamp, timeUUID);
			m_daoMSISDNTimeline.insert(msisdn, timePlusUUID, sCDREntryUuid, StringSerializer.get());
			
			long hourStamp = (timeStamp / MS_PER_HOUR) * MS_PER_HOUR;					
			String sHourStamp = m_sdfHourly.format(new Date(hourStamp));			

			m_daoHourlyTimeline.insert(sHourStamp, timePlusUUID, sCDREntryUuid, StringSerializer.get());						
		} catch(Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		CDRDataAccess cdrAccess = new CDRDataAccess();
		
		// Delete existing keys.
		//cdrAccess.deleteAll();
		//System.out.println("Deleted all data.");

		long currTimeStamp = System.currentTimeMillis() - (MS_PER_HOUR * 24); // Start 24 hrs ago.

		for (int i=0; i < 100; i++) {
			// Test adding entries
			cdrAccess.addCDREntry("14085551212", "MO", "10.10.2.65", "255.255.111.33", 
								  "sachin@geminimobile.com", "gabe@yahoo.com", currTimeStamp, "region1");
			currTimeStamp = currTimeStamp + (MS_PER_HOUR / 4); // 4 cdrs per hour.
			Thread.sleep(10);
		}
	
		//cdrAccess.m_daoMSISDNTimeline.printTable("Entries by MSISDN table");
		
		// Test: 
		long maxTimestamp = System.currentTimeMillis() + 100000L;
		long minstamp = System.currentTimeMillis() - (1000L * 60L * 60L * 24L); // 1 day prior
		
		Vector<CDREntry> vEntries = cdrAccess.getCDRsByMSISDN("14085551212", minstamp, maxTimestamp, "region1", "MO", 500);
		for (CDREntry entry : vEntries) {
			System.out.println("MSISDN Timeline CDREntry: "+entry.msisdn+" "+entry.type+" "+entry.moIPAddress+" "+
							   " "+entry.mtIPAddress+" "+entry.senderDomain+" "+entry.recipientDomain+" "+
							   entry.getDisplayTimestamp());
		}
		
		System.out.println("Get CDRs from Hourly table\n-------------------");
		currTimeStamp = currTimeStamp - (MS_PER_HOUR * 10); // 10 hours earlier
		currTimeStamp = (currTimeStamp / MS_PER_HOUR) * MS_PER_HOUR;
		vEntries = cdrAccess.getCDRsForHour(m_sdfHourly.format(new Date(currTimeStamp)));
		for (CDREntry entry : vEntries) {
			System.out.println("Hourly Timeline CDREntry: "+entry.msisdn+" "+entry.type+" "+entry.moIPAddress+" "+
							   " "+entry.mtIPAddress+" "+entry.senderDomain+" "+entry.recipientDomain+" "+
							   entry.getDisplayTimestamp());
		}

		System.out.println("TEST END ----------- Done.");
		System.exit(0);
	}
}
