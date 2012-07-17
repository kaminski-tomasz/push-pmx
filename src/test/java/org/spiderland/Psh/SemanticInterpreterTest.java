package org.spiderland.Psh;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.spiderland.Psh.Interpreter.StackType;
import org.spiderland.Psh.SemanticInterpreter.InterpreterState;

import ec.util.MersenneTwisterFast;

public class SemanticInterpreterTest {

	SemanticInterpreter interpreter = null;
		
	@Before
	public void prepare() throws RuntimeException, Exception {
		interpreter = new SemanticInterpreter();
		interpreter.Initialize(new MersenneTwisterFast());
		interpreter.SetInstructions(new Program("(float.- float.pop float.dup float.erc float.+ float.- float.* float./)"));
		interpreter.setMaxPointsInProgram(100);
	}
	
//	@Test
	public void no_tracing_test() throws Exception {
		
		interpreter.ClearStacks();
		interpreter._stackToBeTraced = null;
		Program program = new Program("( 1 2 integer.+  )");
		
		interpreter.Execute(program);
		
		assertEquals(new ObjectStack(), interpreter._trace);
	}
	
//	@Test
	public void trace_int_stack() throws Exception {
		
		interpreter.ClearStacks();
		interpreter._stackToBeTraced = StackType.INT_STACK;
		Program program = new Program("( 1 2 integer.+  )");
		
		interpreter.Execute(program);
		
		ObjectStack expectedTrace = new ObjectStack();
		
		expectedTrace.push(new intStack());
		((intStack) expectedTrace.top()).push(1);
		
		expectedTrace.push(new intStack());
		((intStack) expectedTrace.top()).push(1);
		((intStack) expectedTrace.top()).push(2);
		
		expectedTrace.push(new intStack());
		((intStack) expectedTrace.top()).push(3);

		//System.out.println("Expec.: "+expectedTrace);
		//System.out.println("Result: "+interpreter._trace);
		
		assertEquals(expectedTrace, interpreter._trace);
	}
	
//	@Test
	public void trace_bool_stack() throws Exception {
		
		interpreter.ClearStacks();
		interpreter._stackToBeTraced = StackType.BOOL_STACK;
		Program program = new Program("( true true boolean.xor  )");
		
		interpreter.Execute(program);
		
		ObjectStack expectedTrace = new ObjectStack();
		
		expectedTrace.push(new booleanStack());
		((booleanStack) expectedTrace.top()).push(true);
		
		expectedTrace.push(new booleanStack());
		((booleanStack) expectedTrace.top()).push(true);
		((booleanStack) expectedTrace.top()).push(true);

		expectedTrace.push(new booleanStack());
		((booleanStack) expectedTrace.top()).push(false);
		
		//System.out.println("Expec.: "+expectedTrace);
		//System.out.println("Result: "+interpreter._trace);
		
		assertEquals(expectedTrace, interpreter._trace);
	}
	
//	@Test
	public void trace_float_stack() throws Exception {
		
		interpreter.ClearStacks();
		interpreter._stackToBeTraced = StackType.FLOAT_STACK;
		Program program = new Program("( 1 2 integer.+ 1.0 3.0 true float.+ )");
		
		interpreter.Execute(program);
		
		ObjectStack expectedTrace = new ObjectStack();
		
		// first three instructions don't push anything onto float stack
		expectedTrace.push(new floatStack());
		expectedTrace.push(new floatStack());
		expectedTrace.push(new floatStack());
		
		expectedTrace.push(new floatStack());
		((floatStack) expectedTrace.top()).push(1);
		
		expectedTrace.push(new floatStack());
		((floatStack) expectedTrace.top()).push(1);
		((floatStack) expectedTrace.top()).push(3);
		
		// pushing "true" - there's no change in float stack
		expectedTrace.push(new floatStack());
		((floatStack) expectedTrace.top()).push(1);
		((floatStack) expectedTrace.top()).push(3);
		
		expectedTrace.push(new floatStack());
		((floatStack) expectedTrace.top()).push(4);
		
		//System.out.println("Expec.: "+expectedTrace);
		//System.out.println("Result: "+interpreter._trace);
		
		assertEquals(expectedTrace, interpreter._trace);
	}
	
//	@Test
	public void trace_objects_not_same() throws Exception {
		
		interpreter.ClearStacks();
		interpreter._stackToBeTraced = StackType.FLOAT_STACK;
		Program program = new Program("( 1 2 integer.+ 1.0 3.0 true float.+ )");
		
		interpreter.Execute(program);
		
		ObjectStack traceCopy = interpreter._trace.clone();

		assertNotSame(traceCopy, interpreter._trace);
		
//		System.out.println(interpreter._trace);
//		System.out.println(traceCopy);
		assertEquals(true, traceCopy.equals(interpreter._trace));
		
		((floatStack)interpreter._trace.peek(1)).push(12323); // some changes
//		System.out.println(interpreter._trace);
//		System.out.println(traceCopy);
		
		assertNotSame(traceCopy, interpreter._trace);
		assertEquals(false, traceCopy.equals(interpreter._trace));
		
	}
	
//	@Test
	public void trace_nested_programs() throws Exception {
		interpreter.ClearStacks();
		interpreter._stackToBeTraced = StackType.INT_STACK;
		Program program = new Program("( (1 2 integer.+) ( 4 2 integer./) (8 7 integer.-) )");
		
		interpreter.Execute(program);
		
		ObjectStack expectedTrace = new ObjectStack();
		
		expectedTrace.push(new intStack());
		((intStack) expectedTrace.top()).push(3);
		
		expectedTrace.push(new intStack());
		((intStack) expectedTrace.top()).push(3);
		((intStack) expectedTrace.top()).push(2);
		
		expectedTrace.push(new intStack());
		((intStack) expectedTrace.top()).push(3);
		((intStack) expectedTrace.top()).push(2);
		((intStack) expectedTrace.top()).push(1);
		
		//System.out.println("Expec.: "+expectedTrace);
		//System.out.println("Result: "+interpreter._trace);
		
		assertEquals(expectedTrace, interpreter._trace);
	}
	
	@Test
	public void test_list_of_instruction() throws Exception {
		interpreter.ClearStacks();
		
		Program p1 = new Program ("( 7.0 1.0 2.0 float.dup float.dup bleble1 nazwa2 )");
		
		Program sub1 = new Program("( float.+ float.- )");
		
		Program sub2 = new Program("( float.* float./ )");
		
		System.out.println(interpreter);
		
		Program pre = new Program();
		p1.CopyTo(pre);
		System.out.println(pre);
		
		int cutPoint = 5;
		
		int availableSteps = interpreter.getMaxPointsInProgram();
		int stepsTaken = 0;
		
		int steps;
		
		InterpreterState inState = interpreter.new InterpreterState();
		
		steps = interpreter.Execute(p1.Copy(cutPoint), availableSteps);
		stepsTaken += steps;
		availableSteps -= steps;
		if (availableSteps <= 0) {
			System.out.println("warning: available steps = " + availableSteps + " when executing " + p1.Copy(cutPoint));
		}
		
		System.out.println(interpreter);
		inState.save();
		
		
		inState.restore();
		steps = interpreter.Execute(sub1, availableSteps);
		System.out.println(interpreter);
		

		inState.restore();
		steps = interpreter.Execute(sub2, availableSteps);
		System.out.println(interpreter);
		
		
	}
}
