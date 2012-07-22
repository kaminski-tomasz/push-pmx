package org.pushpmx.problem;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import org.ecj.psh.PshEvolutionState;
import org.junit.Before;
import org.junit.Test;
import org.spiderland.Psh.InterpreterState;
import org.spiderland.Psh.Program;
import org.spiderland.Psh.SemanticInterpreter;

import ec.util.MersenneTwisterFast;
import ec.util.Output;
import ec.util.Parameter;
import ec.util.ParameterDatabase;

public class FloatSymbolicRegressionTest {

	Parameter base;
	PshEvolutionState state;
	FloatSymbolicRegression problem;
	ParameterDatabase parameters;
	Output output;
	
	@Before
	public void prepare() {
		
		state = mock(PshEvolutionState.class);
		
		state.random = new MersenneTwisterFast[1];
		state.random[0] = new MersenneTwisterFast();

		output = mock(Output.class);
		state.output  = output;
        doThrow(IllegalStateException.class).when(output).fatal(anyString());
        
        // setting parameters
		parameters = new ParameterDatabase();
		base = new Parameter("base");
		parameters.set(base.push(FloatSymbolicRegression.P_TRAINNUMPOINTS), "3");
		parameters.set(base.push(FloatSymbolicRegression.P_REPEATFLOATSTACK), "3");
		parameters.set(base.push(FloatSymbolicRegression.P_TRAINMINRANGE), "-1");
		parameters.set(base.push(FloatSymbolicRegression.P_TRAINMAXRANGE), "1");
		parameters.set(base.push(FloatSymbolicRegression.P_MAKEINPUTS), "true");
		
		state.parameters = parameters;
		
		// creating problem
		problem = new FloatSymbolicRegression() {
			@Override
			protected double evaluateFunction(double x) {
				return x;
			}
		};
		problem.setup(state, base);		
	}	
	
	@Test
	public void test_training_points() {
		double[][] expected = { { -1, -1 }, { 0, 0 }, { 1, 1 } };
		assertArrayEquals(expected, problem.trainPoints);
	}
	
	@Test
	public void test_init_interpreter_state() {
		InterpreterState[] stateArray = problem.initInterpreterStateArray();
		InterpreterState[] expectedArray = new InterpreterState[] {
				new InterpreterState(),
				new InterpreterState(),
				new InterpreterState(),
		};
		expectedArray[0].getFloatStack().push(-1);
		expectedArray[0].getFloatStack().push(-1);
		expectedArray[0].getFloatStack().push(-1);
		expectedArray[0].getInputStack().push(-1.0f);

		expectedArray[1].getFloatStack().push(0);
		expectedArray[1].getFloatStack().push(0);
		expectedArray[1].getFloatStack().push(0);
		expectedArray[1].getInputStack().push(0.0f);
		
		expectedArray[2].getFloatStack().push(1);
		expectedArray[2].getFloatStack().push(1);
		expectedArray[2].getFloatStack().push(1);
		expectedArray[2].getInputStack().push(1.0f);
	
		assertArrayEquals(expectedArray, stateArray);
	}
	
	@Test
	public void test_compute_state_array() throws Exception {
		InterpreterState[] initArray = problem.initInterpreterStateArray();
		
		SemanticInterpreter interpreter = new SemanticInterpreter();
		interpreter.Initialize(state.random[0]);
		
		Program program = new Program("(2.0 float.*)");
		
		InterpreterState[] resultArray = problem.computeInterpreterStateArray(
				initArray, interpreter, program);
		
		InterpreterState[] expectedArray = new InterpreterState[] {
				new InterpreterState(),
				new InterpreterState(),
				new InterpreterState(),
		};
		expectedArray[0].getFloatStack().push(-1);
		expectedArray[0].getFloatStack().push(-1);
		expectedArray[0].getFloatStack().push(-2);
		expectedArray[0].getInputStack().push(-1.0f);
		expectedArray[0].getCodeStack().push(program);

		expectedArray[1].getFloatStack().push(0);
		expectedArray[1].getFloatStack().push(0);
		expectedArray[1].getFloatStack().push(0);
		expectedArray[1].getInputStack().push(0.0f);
		expectedArray[1].getCodeStack().push(program);
		
		expectedArray[2].getFloatStack().push(1);
		expectedArray[2].getFloatStack().push(1);
		expectedArray[2].getFloatStack().push(2);
		expectedArray[2].getInputStack().push(1.0f);
		expectedArray[2].getCodeStack().push(program);
		
		assertArrayEquals(expectedArray, resultArray);
	}
}
