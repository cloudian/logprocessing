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

package com.geminimobile.util;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import org.apache.log4j.Logger;

public class Configuration {

	public static final String CONFIGURATION_FILE_NAME = "cdrsearch.properties";
	
	private static Properties properties = new Properties();
    private static final Logger logger = Logger.getRootLogger();
	
	static {
		ClassLoader loader = null;
		URL url = null;
		InputStream is = null;

		try {
			loader = Configuration.class.getClassLoader();
			url = loader.getResource(CONFIGURATION_FILE_NAME);

			logger.info("Loading properties file from " + url.toString());
			is = url.openStream();
			properties.load(is);
		}
		catch (FileNotFoundException fnfEx) {
			logger.fatal("Cannot find configuration file " 
					  + url.toString() 
					  + ". Application cannot be startedi (" 
					  + fnfEx.getMessage() 
					  + ")");
			System.exit(-1);
		}
		catch (Exception ex) {
			logger.warn("Cannot load configuration file " 
					 + CONFIGURATION_FILE_NAME 
					 + " Reason:  " 
					 + ex.getMessage() 
					 + ". Application cannot be started");
			logger.fatal("Cannot load configuration file " 
					  + CONFIGURATION_FILE_NAME 
					  + ". Application cannot be started");
			System.exit(-1);
		}
		finally {
			if (is != null) {
				try {
					is.close();
				} 
				catch (Exception ignored) {
					// throw away
				}
			}
		}
	}
	
	public static Properties getProperties() {
		return properties;
	}
	
	public static String get(String key) {
		String value = properties.getProperty(key);
		if(null != value){
			value = value.trim();
			logger.debug("configuration.get ,key = " + key + " value = " + value);
		}
		else{
			logger.error("configuration.get missing parameter from au.properties --> " + key);
		}
        return value;
	}

	public static String get(String key, String defaultVal) {
		String value = properties.getProperty(key);
		if(null != value){
			value = value.trim();
			logger.debug("configuration.get ,key = " + key + " value = " + value);
			
		}
		else{
			logger.error("configuration.get missing parameter from au.properties --> " + key);
			return defaultVal.trim();
		}
		
        return value;
	}
    
}
