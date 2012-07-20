package org.pushpmx.util;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import org.ecj.psh.PshEvolutionState;
import org.junit.Before;
import org.junit.Test;

import ec.util.MersenneTwisterFast;
import ec.util.Output;

public class PermutationTest {

	PshEvolutionState state = null;
	Permutation perm = null;
	private Output stateOutput;
	private MersenneTwisterFast[] stateRandom;

	@Before
	public void prepare() {

		// mock the evolution state because it is too expensive
		// to provide it properly configured
		state = mock(PshEvolutionState.class);
		stateOutput = mock(Output.class);
		stateRandom = new MersenneTwisterFast[1];
		stateRandom[0] = new MersenneTwisterFast();

		// make state output fatal throw a new exception
		doThrow(IllegalStateException.class).when(stateOutput).fatal(
				anyString());

		// mocking the evolution state
		state.output = stateOutput;
		state.random = stateRandom;
	}

	protected void printPerm(Permutation perm) {
		System.out.print("[");
		for (int i = 0; i < perm.perm.length; i++) {
			if (i > 0)
				System.out.print(",");
			System.out.print(perm.perm[i]);
		}
		System.out.println("]");
	}

	@Test
	public void test_new_permutation() {
		Permutation perm = new Permutation(3);
		assertArrayEquals(new int[] { 0, 1, 2 }, perm.perm);
	}

	@Test
	public void test_shuffle_0() {
		Permutation perm = new Permutation(5);
		stateRandom[0].setSeed(1234);
		perm.shuffle(state, 0, 0);
		

		Permutation perm2 = new Permutation(5);
		stateRandom[0].setSeed(1234);
		perm2.shuffle(state, 0, 5);
		
		assertArrayEquals(perm2.perm, perm.perm);	
	}

	@Test
	public void test_shuffle_1() {
		Permutation perm = new Permutation(5);
		perm.shuffle(state, 0, 1);
//		printPerm(perm);	
	}
	
	@Test
	public void test_shuffle_2() {
		Permutation perm = new Permutation(5);
		perm.shuffle(state, 0, 2);
//		printPerm(perm);	
	}
	
	@Test
	public void test_shuffle_3() {
		Permutation perm = new Permutation(5);
		perm.shuffle(state, 0, 3);
//		printPerm(perm);	
	}
	
}
