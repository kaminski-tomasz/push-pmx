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

package org.pushpmx;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.spiderland.Psh.Interpreter.StackType;
import org.spiderland.Psh.InterpreterState;
import org.spiderland.Psh.booleanStack;
import org.spiderland.Psh.floatStack;
import org.spiderland.Psh.intStack;

public class SemanticsTest {

	protected Semantics semantics;
	protected Semantics semantics2;
	
	@Before
	public void prepare() throws Exception {
		semantics = new Semantics();
		semantics2 = new Semantics();
	}

	@Test
	public void test_contruct_from_interpreter_state_boolean_stack() {
		InterpreterState[] state = new InterpreterState[3];
		state[0] = mock(InterpreterState.class);
		when(state[0].getBoolStack()).then(new Answer<booleanStack>() {
			public booleanStack answer(InvocationOnMock invocation)
					throws Throwable {
				booleanStack boolStack = new booleanStack();
				boolStack.push(true);
				boolStack.push(false);
				boolStack.push(true);
				boolStack.push(true);
				return boolStack;
			}
		});
		state[1] = mock(InterpreterState.class);
		when(state[1].getBoolStack()).then(new Answer<booleanStack>() {
			public booleanStack answer(InvocationOnMock invocation)
					throws Throwable {
				booleanStack boolStack = new booleanStack();
				boolStack.push(false);
				boolStack.push(false);
				boolStack.push(true);
				boolStack.push(false);
				return boolStack;
			}
		});
		state[2] = mock(InterpreterState.class);
		when(state[2].getBoolStack()).then(new Answer<booleanStack>() {
			public booleanStack answer(InvocationOnMock invocation)
					throws Throwable {
				booleanStack boolStack = new booleanStack();
				boolStack.push(false);
				boolStack.push(true);
				boolStack.push(true);
				boolStack.push(false);
				return boolStack;
			}
		});
		
		Semantics semantics = new Semantics(state, StackType.BOOL_STACK);
		ArrayList<float[]> expected = new ArrayList<float[]>(3);
		expected.add(new float[]{1, 0, 1, 1});
		expected.add(new float[]{0, 0, 1, 0});
		expected.add(new float[]{0, 1, 1, 0});
		assertArrayEquals(expected.toArray(), semantics.stackVector.toArray());
		for (int i = 0; i < 3; i++) {
			verify(state[i], times(1)).getBoolStack();
			verify(state[i], times(0)).getFloatStack();
			verify(state[i], times(0)).getIntStack();
			verify(state[i], times(0)).getCodeStack();
			verify(state[i], times(0)).getNameStack();
			verify(state[i], times(0)).getInputStack();
		}		
	}
	
	@Test
	public void test_contruct_from_interpreter_state_float_stack() {
		InterpreterState[] state = new InterpreterState[2];
		state[0] = mock(InterpreterState.class);
		when(state[0].getFloatStack()).then(new Answer<floatStack>() {
			public floatStack answer(InvocationOnMock invocation)
					throws Throwable {
				floatStack stack = new floatStack();
				stack.push(1);
				stack.push(2);
				stack.push(3);
				return stack;
			}
		});
		state[1] = mock(InterpreterState.class);
		when(state[1].getFloatStack()).then(new Answer<floatStack>() {
			public floatStack answer(InvocationOnMock invocation)
					throws Throwable {
				floatStack stack = new floatStack();
				stack.push(3.2f);
				stack.push(3.33f);
				return stack;
			}
		});
		
		Semantics semantics = new Semantics(state);
		ArrayList<float[]> expected = new ArrayList<float[]>(2);
		expected.add(new float[]{1.0f, 2.0f, 3.0f});
		expected.add(new float[]{3.2f, 3.33f});
		assertArrayEquals(expected.toArray(), semantics.stackVector.toArray());
		for (int i = 0; i < 2; i++) {
			verify(state[i], times(0)).getBoolStack();
			verify(state[i], times(1)).getFloatStack();
			verify(state[i], times(0)).getIntStack();
			verify(state[i], times(0)).getCodeStack();
			verify(state[i], times(0)).getNameStack();
			verify(state[i], times(0)).getInputStack();
		}		
	}
	
	@Test
	public void test_contruct_from_interpreter_state_int_stack() {
		InterpreterState[] state = new InterpreterState[2];
		state[0] = mock(InterpreterState.class);
		when(state[0].getIntStack()).then(new Answer<intStack>() {
			public intStack answer(InvocationOnMock invocation)
					throws Throwable {
				intStack stack = new intStack();
				stack.push(1);
				stack.push(2);
				stack.push(3);
				return stack;
			}
		});
		state[1] = mock(InterpreterState.class);
		when(state[1].getIntStack()).then(new Answer<intStack>() {
			public intStack answer(InvocationOnMock invocation)
					throws Throwable {
				intStack stack = new intStack();
				stack.push(3);
				stack.push(1);
				return stack;
			}
		});
		
		Semantics semantics = new Semantics(state, StackType.INT_STACK);
		ArrayList<float[]> expected = new ArrayList<float[]>(2);
		expected.add(new float[]{1.0f, 2.0f, 3.0f});
		expected.add(new float[]{3.0f, 1.0f});
		assertArrayEquals(expected.toArray(), semantics.stackVector.toArray());
		for (int i = 0; i < 2; i++) {
			verify(state[i], times(0)).getBoolStack();
			verify(state[i], times(0)).getFloatStack();
			verify(state[i], times(1)).getIntStack();
			verify(state[i], times(0)).getCodeStack();
			verify(state[i], times(0)).getNameStack();
			verify(state[i], times(0)).getInputStack();
		}		
	}
	
	@Test
	public void test_add_float_stack() {
		
		assertNotNull(semantics.stackVector);
		semantics.stackVector.clear();
		assertEquals(semantics.stackVector.size(), 0);
		
		floatStack stack = new floatStack();
		stack.push(0.0f);
		stack.push(1.0f);
		stack.push(12.05f);
		semantics.addFloatStack(stack);
		assertEquals(semantics.stackVector.size(), 1);
		assertArrayEquals(semantics.stackVector.get(0), new float[] { 0.0f,
				1.0f, 12.05f }, 0);

		stack = new floatStack();
		stack.push(1);
		stack.push(2);
		semantics.addFloatStack(stack);
		assertEquals(semantics.stackVector.size(), 2);
		assertArrayEquals(semantics.stackVector.get(1), new float[] { 1.0f,
				2.0f }, 0);
		
		stack = new floatStack();
		semantics.addFloatStack(stack);
		assertEquals(semantics.stackVector.size(), 3);
		assertArrayEquals(semantics.stackVector.get(2), new float[0], 0);
		
	}
	
	@Test
	public void test_add_int_stack() {

		assertNotNull(semantics.stackVector);
		semantics.stackVector.clear();
		assertEquals(semantics.stackVector.size(), 0);
		
		intStack stack = new intStack();
		stack.push(0);
		stack.push(1);
		stack.push(12);
		semantics.addIntStack(stack);
		assertEquals(semantics.stackVector.size(), 1);
		assertArrayEquals(semantics.stackVector.get(0), new float[] { 0.0f,
				1.0f, 12.0f }, 0.0f);

		stack = new intStack();
		stack.push(1);
		stack.push(2);
		semantics.addIntStack(stack);
		assertEquals(semantics.stackVector.size(), 2);
		assertArrayEquals(semantics.stackVector.get(1), new float[] { 1.0f,
				2.0f }, 0.0f);
		
		stack = new intStack();
		semantics.addIntStack(stack);
		assertEquals(semantics.stackVector.size(), 3);
		assertArrayEquals(semantics.stackVector.get(2), new float[0], 0.0f);
		
	}
	
	@Test
	public void test_add_boolean_stack() {

		assertNotNull(semantics.stackVector);
		semantics.stackVector.clear();
		assertEquals(semantics.stackVector.size(), 0);
		
		booleanStack stack = new booleanStack();
		stack.push(true);
		stack.push(false);
		stack.push(true);
		semantics.addBooleanStack(stack);
		assertEquals(semantics.stackVector.size(), 1);
		assertArrayEquals(semantics.stackVector.get(0), new float[] { 1.0f,
				0.0f, 1.0f }, 0.0f);
		
		stack = new booleanStack();
		stack.push(false);
		stack.push(true);
		semantics.addBooleanStack(stack);
		assertEquals(semantics.stackVector.size(), 2);
		assertArrayEquals(semantics.stackVector.get(1), new float[] { 0.0f,
				1.0f }, 0.0f);
		
		stack = new booleanStack();
		semantics.addBooleanStack(stack);
		assertEquals(semantics.stackVector.size(), 3);
		assertArrayEquals(semantics.stackVector.get(2), new float[0], 0.0f);
		
	}
	
	@Test
	public void test_are_equal() {
		

		assertNotNull(semantics.stackVector);
		semantics.stackVector.clear();
		assertEquals(semantics.stackVector.size(), 0);
		
		assertNotNull(semantics2.stackVector);
		semantics2.stackVector.clear();
		assertEquals(semantics2.stackVector.size(), 0);

		// sem1
		floatStack stack = new floatStack();
		stack.push(0.0f);
		stack.push(1.0f);
		stack.push(12.05f);
		semantics.addFloatStack(stack);
		
		stack = new floatStack();
		stack.push(3.0f);
		stack.push(1.0f);
		stack.push(12.05f);
		semantics.addFloatStack(stack);
		
		// sem2
		stack = new floatStack();
		stack.push(0.0f);
		stack.push(1.0f);
		stack.push(12.05f);
		semantics2.addFloatStack(stack);
		
		stack = new floatStack();
		stack.push(3.0f);
		stack.push(1.0f);
		stack.push(12.05f);
		semantics2.addFloatStack(stack);
		
		assertEquals(semantics.stackVector.size(), 2);
		assertEquals(semantics2.stackVector.size(), 2);
		
		assertArrayEquals(semantics.stackVector.get(0), new float[]{0,1,12.05f}, 0.0f);
		assertArrayEquals(semantics.stackVector.get(1), new float[]{3,1,12.05f}, 0.0f);

		assertArrayEquals(semantics2.stackVector.get(0), new float[]{0,1,12.05f}, 0.0f);
		assertArrayEquals(semantics2.stackVector.get(1), new float[]{3,1,12.05f}, 0.0f);
		
		
		assertTrue("equality of semantics", semantics.equals(semantics2));
		
		
	}
	
}
