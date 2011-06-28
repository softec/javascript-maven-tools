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

package searls.jasmine.model;


public class JasmineResult {
	private String description;
	private String details;
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean didPass() {
		if(description == null) {
			throw new IllegalStateException("Can only determine success after description is set.");
		}
		return description.contains("0 failures");
	}
	
	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}
	
}
