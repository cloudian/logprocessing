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


import org.apache.log4j.Logger;


// Cleanup data after xx days
public class Cleanup {
	private static final Logger logger = Logger.getLogger(Cleanup.class);
	public static final long MS_PER_DAY = 86400000L;
	public static final long MS_PER_HOUR = 3600000L;

	public static void deleteRecords(int nDaysToRetain) {
		CDRDataAccess cdrData = new CDRDataAccess();
		RawCdrAccess rawData = new RawCdrAccess();
		
		if (nDaysToRetain < 0) {
			logger.info("Not processing Cleanup - Days To Retain param is negative.");			
		}
		else {
			long deleteBeforeMS = System.currentTimeMillis() - (nDaysToRetain * MS_PER_DAY);
			deleteBeforeMS = (deleteBeforeMS / MS_PER_HOUR) * MS_PER_HOUR;
			cdrData.deleteBeforeTS(deleteBeforeMS);
			rawData.open();
			rawData.deleteBeforeTS(deleteBeforeMS);			
			rawData.close();
		}
	}
	
    public static void main(String[] args) {

    	if (args.length != 2) {
    		System.out.println("Incorrect number of arguments for cleanup.  Argument must be: -d <#days to retain>");
    	}
    	if (args[0].equals("-d")) {
    		String strDays = args[1];
    		int days = Integer.parseInt(strDays);
    		
    		Cleanup.deleteRecords(days);
    		
    	} 
    	else {
    		System.out.println("Unrecognized option: " + args[0] + ". Must use -d <#days to retain>");
    	}
        
    	System.exit(0);
    }
}
