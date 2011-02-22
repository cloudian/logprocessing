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

/**
 * For storing an option code/value to be displayed in a drop down box in the U/I.
 * @author snaik
 *
 */
public class Option
{
	private String code;
	private String label;

	public Option() { };			
	public Option(String c, String l) 
	{
		this.code = c;
		this.label = l;
	}			

	public String getCode() 
	{
		return code;
	}
	public void setCode(String code) 
	{
		this.code = code;
	}
	public String getLabel() 
	{
		return label;
	}
	public void setLabel(String label) 
	{
		this.label = label;
	}					
}
