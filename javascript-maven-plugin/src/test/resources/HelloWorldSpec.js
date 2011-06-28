/*
 * Copyright 2010 SOFTEC sa. All rights reserved.
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

describe('HelloWorld',function(){
	
	it('should say hello',function(){
		var helloWorld = new HelloWorld();
		expect(helloWorld.greeting()).toBe("Hello, World");
	});
	
	//Intentional fail:
	it('should say goodbye',function(){
		var helloWorld = new HelloWorld();
		expect(helloWorld.greeting()).toBe("Goodbye, World");
	});
	
	it('should fail',function() {
		expect(5).toBe(6);
	});
	
});
