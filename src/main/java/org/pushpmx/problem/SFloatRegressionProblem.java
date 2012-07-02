package org.pushpmx.problem;

import org.ecj.psh.PshEvaluator;
import org.ecj.psh.PshIndividual;
import org.ecj.psh.problem.FloatRegressionProblem;
import org.pushpmx.PSIndividual;
import org.spiderland.Psh.Interpreter;
import org.spiderland.Psh.Program;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.koza.KozaFitness;

public class SFloatRegressionProblem extends FloatRegressionProblem {

	private float evaluateTestCase(Interpreter interpreter, PSIndividual ind,
			float input, float output) {

		interpreter.ClearStacks();

		// pushing input value to float stack
		for (int i = 0; i < repeatFloatStack; i++) {
			interpreter.floatStack().push(input);
		}

		// setting input value to input stack
		interpreter.inputStack().push((Float) input);

		// executing the program
		interpreter.Execute(ind.program, interpreter.getExecutionLimit());

		// *** set the semantics for this test case
		ind.semantics.addFloatStack(interpreter.floatStack());
		
		// Penalize individual if there is no result on the stack.
		if (interpreter.floatStack().size() == 0) {
			return 1000.0f;
		}

		// compute result as absolute difference
		float error = Math.abs(interpreter.floatStack().top() - output);

		return error;
	}

	@Override
	public void evaluate(EvolutionState state, Individual ind,
			int subpopulation, int threadnum) {

		if (ind.evaluated)
			return;

		if (!(ind instanceof PSIndividual)) {
			state.output.fatal("This is not PSIndividual instance!");
		}

		Interpreter interpreter = ((PshEvaluator) state.evaluator).interpreter[threadnum];
		Program program = ((PSIndividual) ind).program;

		float fitness = 0.0f;
		int hits = 0;

		for (Float[] testCase : testCases) {
			float input = testCase[0];
			float output = testCase[1];

			float error = evaluateTestCase(interpreter, (PSIndividual)ind, input, output);

			if (error < 0.01)
				hits++;
			fitness += error;
		}
		if (Float.isInfinite(fitness)) {
			fitness = Float.MAX_VALUE;
		} else {
			// compute mean absolute error
			fitness = fitness / (float) testCases.size();
		}

		KozaFitness f = (KozaFitness) ind.fitness;
		f.setStandardizedFitness(state, fitness);
		f.hits = hits;
		ind.evaluated = true;
	}

}
