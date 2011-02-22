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
import java.util.Properties;

import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.geminimobile.util.Configuration;

public class RawCdrAccess {

    private static final Logger log = LoggerFactory.getLogger(RawCdrAccess.class);
    public static final String HourlyTimeStampFormat = "yyyyMMddHH"; 
    public static SimpleDateFormat m_sdf = new SimpleDateFormat(HourlyTimeStampFormat); 

    public static final String KEYSPACE = "CDRLogs";
    public static final String RAW_CDR_CF_PREFIX = "RawCDREntry_";
    private static final int HECTOR_MAX_RECORDS = 1023;

    private String[] markets = {"region1", "region2", "region3", "region4"};
    private DataAccessObject[] rawTableObjects = null;
    private String keyAnchor;
    private Properties conf;

    public RawCdrAccess() {    
        // @todo(gki): read conf from prop
        keyAnchor = "";
    }

    public void close()
    {
       // todo(gki): from hector v2, need to shutdown cluster
    }

    public void reset()
    {
        keyAnchor = "";
    }

    // Create a DAO for each market's raw table
    public void open()
    {
        rawTableObjects = new DataAccessObject[markets.length]; // New array of data access objects, one for each market
        
        if (conf == null)
            conf = Configuration.getProperties();
        
        // todo(gki): Sachin, this could be comma sparated list of hosts
        String host = conf.getProperty("hosts", "localhost");
        String portStr = conf.getProperty("port", "9160");

        int port = Integer.parseInt(portStr);
        
        for (int i = 0; i < markets.length; i++) {
            rawTableObjects[i] = new DataAccessObject(KEYSPACE, host, port,
                    RAW_CDR_CF_PREFIX + markets[i]);
        }
    }

    public void setProperties(Properties p)
    {
        conf = new Properties(p);
    }

    public List<String> getRawCdr(int market, String timestamp)
    {
        return getRawCdr(market, timestamp, keyAnchor, HECTOR_MAX_RECORDS);
    }

    public List<String> getRawCdr(int market, String timestamp, String startUuid, int limit)
    {
        // indicate reaching the end in the previous run
        if (startUuid == null)
            return new ArrayList<String>(0);

        DataAccessObject dao = rawTableObjects[market];        
        ArrayList<String> rawCdrs = new ArrayList<String>(limit);
        List<HColumn<String, String>> columns = dao.getSlice(timestamp, startUuid, "", limit + 1);

        for (HColumn<String, String> column : columns) {
            if (--limit > 0) {
                rawCdrs.add(column.getValue());
                log.warn("getting... " + column.getName());
            } else {
                // next starting point
                keyAnchor = column.getName();
            }
        }

        if (limit > 0)
            keyAnchor = null;      

        return rawCdrs;
    }

    public void deleteBeforeTS(long hourStamp) {
        String sLastHour = m_sdf.format(new Date(hourStamp));

        // Clean up old data from Raw tables
        for (DataAccessObject dao: rawTableObjects) {
            String begRangeKey = "";
            boolean bMoreRows = true;
            
            while (bMoreRows) {
                List<Row<String, String, String>> rows = dao.getRangeSlice(begRangeKey, sLastHour, "", HECTOR_MAX_RECORDS,false);

                if (rows.size() > 0) {
                	Row<String, String, String> lastRow = rows.get(rows.size() - 1);
                	begRangeKey = lastRow.getKey();

                    for (Row<String, String, String> row : rows) {
                        dao.delete(row.getKey(), null, StringSerializer.get()); // delete entire row.
                    }
                    if (rows.size() == 1) {
                    	// we are done deleting
                    	bMoreRows = false;
                    }
                } else {
                    bMoreRows = false;
                }
            }
        }        
    }

    public void setMarket(String[] markets) {
        this.markets = markets;
    }

    public int numMarkets() {
        if (markets == null)
            return 0;
        else
            return markets.length;
    }

    public String[] getMarkets() {
        return markets;
    }

    public String getMarket(int i) {
        return markets[i];
    }
}
