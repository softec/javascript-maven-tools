/*
 * Derivative Work
 * Copyright 2010 SOFTEC sa. All rights reserved.
 *
 * Original Work
 * Copyright 2010 Justin Searls
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package searls.jasmine.format;

import org.apache.maven.plugin.logging.Log;

import searls.jasmine.model.JasmineResult;

public class JasmineResultLogger {

	public static final String HEADER="\n"+
		"-------------------------------------------------------\n"+
		" J A S M I N E   T E S T S\n"+
		"-------------------------------------------------------";
	public static final String FAIL_APPENDAGE = " <<< FAILURE!";
	public static final String INDENT = "  ";
	
	private Log log;

	public void setLog(Log log) {
		this.log = log;
	}

	public void log(JasmineResult result) {
		log.info(HEADER);
		
		log.info(result.getDetails());

		log.info("\nResults:\n\n"+result.getDescription()+"\n");		
	}

}
