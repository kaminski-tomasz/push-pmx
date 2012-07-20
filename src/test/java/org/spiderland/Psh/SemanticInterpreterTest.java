package org.spiderland.Psh;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import ec.util.MersenneTwisterFast;

public class SemanticInterpreterTest {

	SemanticInterpreter interpreter = null;

	@Before
	public void prepare() throws RuntimeException, Exception {
		interpreter = new SemanticInterpreter();
		interpreter.Initialize(new MersenneTwisterFast());
		interpreter.SetRandomParameters(-10, 10, 1, -10, 10, 0.01f, 100, 100);
	}

	@Test
	public void test_generate_all_programs_size_0() throws RuntimeException,
			Exception {
		interpreter.SetInstructions(new Program("(float.+ float.- )"));
		ArrayList<Program> set = interpreter.generateAllPrograms(0);
		assertEquals(new ArrayList<Program>(), set);
	}

	@Test
	public void test_generate_all_programs_size_1() throws RuntimeException,
			Exception {
		interpreter
				.SetInstructions(new Program("(float.+ float.- integer.dup)"));

		ArrayList<Program> set = interpreter.generateAllPrograms(1);
		ArrayList<Program> expected = new ArrayList<Program>(3);
		expected.add(new Program("(float.+)"));
		expected.add(new Program("(float.-)"));
		expected.add(new Program("(integer.dup)"));
		assertEquals(expected, set);
	}

	@Test
	public void test_generate_all_programs_size_2() throws RuntimeException,
			Exception {
		interpreter
				.SetInstructions(new Program("(float.+ float.- integer.dup)"));

		ArrayList<Program> set = interpreter.generateAllPrograms(2);
		ArrayList<Program> expected = new ArrayList<Program>();
		expected.add(new Program("(float.+ float.+)"));
		expected.add(new Program("(float.+ float.-)"));
		expected.add(new Program("(float.+ integer.dup)"));

		expected.add(new Program("(float.- float.+)"));
		expected.add(new Program("(float.- float.-)"));
		expected.add(new Program("(float.- integer.dup)"));

		expected.add(new Program("(integer.dup float.+)"));
		expected.add(new Program("(integer.dup float.-)"));
		expected.add(new Program("(integer.dup integer.dup)"));
		assertEquals(expected, set);
	}

	@Test
	public void test_generate_all_programs_size_3() throws RuntimeException,
			Exception {
		interpreter
				.SetInstructions(new Program(
						"(float.* float.+ float.- float./ float.sin float.cos " +
						"float.ln float.exp float.dup float.flush float.pop " +
						"float.rot float.shove float.stackdepth float.erc float.swap float.yank " +
						"float.yankdup input.makeinputs1)"));

		ArrayList<Program> programs = interpreter.generateAllPrograms(3, true);
		
		int size = interpreter._randomGenerators.size();
		assertEquals(size*size*size, programs.size());
	}

}
