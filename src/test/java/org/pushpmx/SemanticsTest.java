package org.pushpmx;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.spiderland.Psh.booleanStack;
import org.spiderland.Psh.floatStack;
import org.spiderland.Psh.intStack;

public class SemanticsTest {

	protected Semantics semantics;
	
	@Before
	public void prepare() {
		semantics = new Semantics();
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
	
}
