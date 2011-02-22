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


import org.springframework.validation.Errors;
import org.springframework.validation.Validator;



/**
 * Command validator for SearchCommand. 
 * 
 * @author snaik
 */
public class SearchValidator implements Validator
{
    @SuppressWarnings("unchecked")
	public boolean supports(Class clazz) 
    {
        return clazz.equals(SearchCommand.class);
    }

    public void validate(Object obj, Errors errors) 
    {
    	SearchCommand cmd = (SearchCommand) obj;
        
    }

}
