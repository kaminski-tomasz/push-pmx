package org.pushpmx.breed;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.ecj.psh.PshEvaluator;
import org.ecj.psh.PshEvolutionState;
import org.ecj.psh.PshIndividual;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.spiderland.Psh.Interpreter;
import org.spiderland.Psh.Program;

import ec.util.MersenneTwisterFast;
import ec.util.Output;

public class SemanticCrossoverTest {

	PshIndividual[] parents = null;
	PartiallyMedialCrossover semanticCrossover;
	protected PshEvolutionState state;
	protected Output stateOutput;
	protected MersenneTwisterFast[] stateRandom;
	protected PshEvaluator stateEvaluator;
	protected Interpreter interpreter;
	protected int thread;
	
	@Before
	public void prepare() {
		// mock the evolution state because it is too expensive
		// to provide it properly configured
		state = mock(PshEvolutionState.class);
		stateOutput = mock(Output.class);
		stateRandom = new MersenneTwisterFast[1];
		stateRandom[0] = new MersenneTwisterFast();
		stateEvaluator = mock(PshEvaluator.class);
		interpreter = new Interpreter();
		thread = 0;

		// make state output fatal throw a new exception
		doThrow(IllegalStateException.class).when(stateOutput).fatal(
				anyString());

		// mocking the evolution state
		state.output = stateOutput;
		state.random = stateRandom;
		state.evaluator = stateEvaluator;

		state.interpreter = new Interpreter[1];
		state.interpreter[0] = interpreter;

		// setting up operator
		semanticCrossover = new PartiallyMedialCrossover();

		parents = new PshIndividual[2];
		parents[0] = new PshIndividual();
		parents[1] = new PshIndividual();

		// safe limit for program length
		semanticCrossover.setParents(parents);

	}

	@Test
	public void test_decompose_program_no1() throws Exception {
		semanticCrossover.replacementLength = 2;
		Program[] parts = semanticCrossover.decomposeProgram(new Program(
				"(float.+ float.- 1 2 3 A B C)"), 8, 2);
		assertArrayEquals(new Program[] { 
				new Program("(float.+ float.-)"),
				new Program("(1 2)"), 
				new Program("(3 A B C)") }, 
				parts);
	}
	
	@Test
	public void test_decompose_program_no2() throws Exception {
		semanticCrossover.replacementLength = 1;
		Program[] parts = semanticCrossover.decomposeProgram(new Program(
				"(float.+ (float.- 1) 2 3 A B C)"), 7, 1);
		assertArrayEquals(new Program[] { 
				new Program("(float.+)"),
				new Program("((float.- 1))"), 
				new Program("(2 3 A B C)") }, 
				parts);
	}

	@Test
	public void test_join_parts_no1() throws Exception {
		semanticCrossover.replacementLength = 2;

		Program[] parts = new Program[] { 
				new Program("(float.+ float.-)"),
				new Program("(1 2)"), 
				new Program("(3 (A B) C)") };

		Program joined = new Program();
		semanticCrossover.joinDecomposedProgram(parts, joined);
		assertEquals(new Program("(float.+ float.- 1 2 3 (A B) C)"), joined);
	}
	
	@Test
	public void test_join_parts_no2() throws Exception {
		semanticCrossover.replacementLength = 2;

		Program[] parts = new Program[] { 
				new Program("((float.+ float.-))"),
				new Program("(1 2)"), 
				new Program("(3 (A B) C)") };

		Program joined = new Program();
		semanticCrossover.joinDecomposedProgram(parts, joined);
		assertEquals(new Program("((float.+ float.-) 1 2 3 (A B) C)"), joined);
	}
	
	@Test
	public void test_generate_cutting_points_rand_minimum() {
		MersenneTwisterFast oldTwister = state.random[0]; 
		MersenneTwisterFast fakeTwister = mock(MersenneTwisterFast.class);
		state.random[0] = fakeTwister;
		
		when(fakeTwister.nextInt(anyInt())).then(new Answer<Integer>() {
			@Override
			public Integer answer(InvocationOnMock invocation) throws Throwable {
				return 0;
			}
		});
		int size, cutpoint;
		cutpoint = semanticCrossover.findCuttingPoint(state, thread,
				size = 0);
		assertTrue(cutpoint >= 0
				&& cutpoint + semanticCrossover.replacementLength <= size);
		cutpoint = semanticCrossover.findCuttingPoint(state, thread,
				size = 1);
		assertTrue(cutpoint >= 0
				&& cutpoint + semanticCrossover.replacementLength <= size);
		cutpoint = semanticCrossover.findCuttingPoint(state, thread,
				size = 15);
		assertTrue(cutpoint >= 0
				&& cutpoint + semanticCrossover.replacementLength <= size);		
		
		state.random[0] = oldTwister;
	}

	@Test
	public void test_generate_cutting_points_rand_maximum() {
		MersenneTwisterFast oldTwister = state.random[0]; 
		MersenneTwisterFast fakeTwister = mock(MersenneTwisterFast.class);
		state.random[0] = fakeTwister;
		
		int size, cutpoint;
		when(fakeTwister.nextInt(anyInt())).then(new Answer<Integer>() {
			@Override
			public Integer answer(InvocationOnMock invocation) throws Throwable {
				Integer arg = (Integer)invocation.getArguments()[0];
				return arg-1;
			}
		});
		cutpoint = semanticCrossover.findCuttingPoint(state, thread,
				size = 0);
		assertTrue(cutpoint >= 0
				&& cutpoint + semanticCrossover.replacementLength <= size);
		cutpoint = semanticCrossover.findCuttingPoint(state, thread,
				size = 1);
		assertTrue(cutpoint >= 0
				&& cutpoint + semanticCrossover.replacementLength <= size);
		cutpoint = semanticCrossover.findCuttingPoint(state, thread,
				size = 15);
		assertTrue(cutpoint >= 0
				&& cutpoint + semanticCrossover.replacementLength <= size);
		state.random[0] = oldTwister;
	}
	
}
