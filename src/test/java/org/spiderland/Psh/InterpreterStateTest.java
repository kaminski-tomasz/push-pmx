/*
* Copyright 2012 Tomasz Kamiński
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.spiderland.Psh;

import static org.junit.Assert.*;

import org.junit.*;

import ec.util.MersenneTwisterFast;

public class InterpreterStateTest {

	@Before
	public void prepare() {
	}

	@Test
	public void test_constructor() {
		InterpreterState state = new InterpreterState();
		assertEquals(new intStack(), state.getIntStack());
		assertEquals(new floatStack(), state.getFloatStack());
		assertEquals(new booleanStack(), state.getBoolStack());
		assertEquals(new ObjectStack(), state.getInputStack());
		assertEquals(new ObjectStack(), state.getCodeStack());
		assertEquals(new ObjectStack(), state.getNameStack());
	}
	
	@Test
	public void test_clone() throws Exception {
		SemanticInterpreter interpreter = new SemanticInterpreter ();
		interpreter.Initialize(new MersenneTwisterFast());
		interpreter.Execute(new Program("(1.0 2.0 float.+ 1 2 3 true)"));
		
		InterpreterState state = interpreter.getMemoryState().clone();
		
		booleanStack expectedBooleanStack = new booleanStack();
		expectedBooleanStack.push(true);
		assertEquals(expectedBooleanStack, state.getBoolStack());
		assertNotSame(interpreter.boolStack(), state.getBoolStack());
		assertNotSame(expectedBooleanStack, state.getBoolStack());
		
		floatStack expectedFloatStack = new floatStack();
		expectedFloatStack.push(3);
		assertEquals(expectedFloatStack, state.getFloatStack());
		assertNotSame(interpreter.floatStack(), state.getFloatStack());
		assertNotSame(expectedFloatStack, state.getFloatStack());
		
		intStack expectedIntStack = new intStack();
		expectedIntStack.push(1);
		expectedIntStack.push(2);
		expectedIntStack.push(3);
		assertEquals(expectedIntStack, state.getIntStack());
		assertNotSame(interpreter.intStack(), state.getIntStack());
		assertNotSame(expectedIntStack, state.getIntStack());
		
		ObjectStack expectedCodeStack = new ObjectStack();
		expectedCodeStack.push(new Program("(1.0 2.0 float.+ 1 2 3 true)"));
		assertEquals(expectedCodeStack, state.getCodeStack());
		assertNotSame(interpreter.codeStack(), state.getCodeStack());
		assertNotSame(expectedCodeStack, state.getCodeStack());
	}

}
