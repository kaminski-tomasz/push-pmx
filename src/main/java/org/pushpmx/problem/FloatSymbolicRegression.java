package org.pushpmx.problem;

import org.ecj.psh.PshEvaluator;
import org.ecj.psh.PshIndividual;
import org.ecj.psh.PshProblem;
import org.pushpmx.SemanticIndividual;
import org.spiderland.Psh.Interpreter;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.koza.KozaFitness;
import ec.util.Parameter;

public abstract class FloatSymbolicRegression extends PshProblem {

	public static final String P_REPEATFLOATSTACK = "repeat-float-stack";

	public static final String P_TRAINMINRANGE = "train-min-range";
	public static final String P_TRAINMAXRANGE = "train-max-range";
	public static final String P_TRAINNUMPOINTS = "train-points";

	public static final String P_TESTMINRANGE = "test-min-range";
	public static final String P_TESTMAXRANGE = "test-max-range";
	public static final String P_TESTNUMPOINTS = "test-points";

	public static final String P_TESTPOINTSRES = "test-points-resolution";
	public static final String P_MAKEINPUTS = "make-inputs";			
	public static final String P_HITTHRESHOLD = "hit-threshold";
	
	/** How many times should input number be duplicated in float stack */
	public int repeatFloatStack;

	/** Lower range of the train set */
	public float trainMinRange;

	/** Upper range of the train set */
	public float trainMaxRange;

	/** Number of test cases used in evolution */
	public int numOfTrainPoints;

	/** Lower range of the test set */
	public float testMinRange;

	/** Upper range of the test set */
	public float testMaxRange;

	/** (Optional) resolution of test points */
	public float testPointsResolution;
	
	/** Number of test cases used in testing the evolved solution */
	public int numOfTestPoints;

	/** Value under which individual hits the test case */
	public float hitThreshold;

	/** Should input value be pushed onto input stack? */
	public boolean makeInputs;
	
	/** Points generated to be the training set of test-cases */
	private float[][] trainPoints = null;
	
	@Override
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);
		Parameter def = this.defaultBase();

		repeatFloatStack = state.parameters.getIntWithDefault(
				base.push(P_REPEATFLOATSTACK), def.push(P_REPEATFLOATSTACK), 1);

		trainMinRange = state.parameters.getFloatWithDefault(
				base.push(P_TRAINMINRANGE), def.push(P_TRAINMINRANGE), -1.0f);
		trainMaxRange = state.parameters.getFloatWithDefault(
				base.push(P_TRAINMAXRANGE), def.push(P_TRAINMAXRANGE), 1.0f);
		
		if (trainMaxRange < trainMinRange) {
			state.output.fatal("Upper range should be greater than lower range", base.push(P_TRAINMAXRANGE));
		}
		
		numOfTrainPoints = state.parameters.getIntWithDefault(
				base.push(P_TRAINNUMPOINTS), def.push(P_TRAINNUMPOINTS), 20);

		if (numOfTrainPoints < 2) {
			state.output.fatal("Not enough training points", base.push(P_TRAINNUMPOINTS));
		}
		
		testMinRange = state.parameters.getFloatWithDefault(
				base.push(P_TESTMINRANGE), def.push(P_TESTMINRANGE), -1.0f);
		testMaxRange = state.parameters.getFloatWithDefault(
				base.push(P_TESTMAXRANGE), def.push(P_TESTMAXRANGE), 1.0f);
		
		if (testMaxRange < testMinRange) {
			state.output.fatal("Upper range should be greater than lower range", base.push(P_TESTMAXRANGE));
		}
		
		numOfTestPoints = state.parameters.getIntWithDefault(
				base.push(P_TESTNUMPOINTS), def.push(P_TESTNUMPOINTS), 20);
		
		if (numOfTestPoints < 1) {
			state.output.fatal("Not enough test points", base.push(P_TESTNUMPOINTS));
		}

		testPointsResolution = state.parameters.getFloatWithDefault(
				base.push(P_TESTPOINTSRES), def.push(P_TRAINMAXRANGE), 0.0f);
		
		hitThreshold = state.parameters.getFloatWithDefault(
				base.push(P_HITTHRESHOLD), def.push(P_HITTHRESHOLD), 0.01f);
		
		makeInputs = state.parameters.getBoolean(base.push(P_MAKEINPUTS),
				def.push(P_MAKEINPUTS), false);
				
		// Generating the train set
		state.output.message("Train set test cases: ");
		trainPoints = new float[numOfTrainPoints][];
		for (int i = 0; i < numOfTrainPoints; i++) {
			trainPoints[i] = new float[2];

			trainPoints[i][0] = trainMinRange + i
					* (trainMaxRange - trainMinRange) / (numOfTrainPoints - 1);
			trainPoints[i][1] = evaluateFunction(trainPoints[i][0]);
			state.output.message("[ " + trainPoints[i][0] + ", " + trainPoints[i][1] + " ]"); 
		}
	}
	
	@Override
	public void evaluate(EvolutionState state, Individual ind,
			int subpopulation, int threadnum) {
		if (ind.evaluated)
			return;

		if (!(ind instanceof SemanticIndividual)) {
			state.output.fatal("This is not SemanticIndividual instance!");
		}
		Interpreter interpreter = ((PshEvaluator) state.evaluator).interpreter[threadnum];
		evaluateTrainSet(state, threadnum, interpreter, (SemanticIndividual) ind);
	}

	/**
	 * Set the fitness and hits value for the individual
	 * @param ind
	 * @param fitness
	 * @param hits
	 */
	public void setFitness(EvolutionState state, int thread, SemanticIndividual ind,
			float fitness, int hits) {
		KozaFitness f = (KozaFitness) ind.fitness;
		f.setStandardizedFitness(state, fitness);
		f.hits = hits;
		ind.evaluated = true;
	}
	
	/**
	 * Evaluate the individual for training set points
	 * @param interpreter
	 * @param ind_minRandomFloat
	 */
	public void evaluateTrainSet(EvolutionState state, int thread,
			Interpreter interpreter, SemanticIndividual ind) {
		float errorSum = 0.0f;
		int hits = 0;
		for (int i = 0; i < numOfTrainPoints; i++) {
			float input = trainPoints[i][0];
			float output = trainPoints[i][1];
			float error = evaluateSingleTestCase(interpreter, ind, i, input,
					output);
			if (error < hitThreshold) {
				hits++;
			}
		}
		float fitness;
		if (Float.isInfinite(errorSum)) {
			fitness = Float.MAX_VALUE;
		} else {
			fitness = errorSum / numOfTrainPoints;
		}
		setFitness(state, thread, ind, fitness, hits);
	}
	
	/**
	 * Evaluate the individual for test set points chosen randomly
	 * @param interpreter
	 * @param ind
	 */
	public void evaluateTestSet(EvolutionState state, int thread,
			Interpreter interpreter, SemanticIndividual ind) {
		float errorSum = 0.0f;
		int hits = 0;
		for (int i = 0; i < numOfTestPoints; i++) {
			// generate random point in given range
			float input = state.random[thread].nextFloat()
					* (testMaxRange - testMinRange);
			if (testPointsResolution > 0.0f) {
				input %= testPointsResolution;
			}
			input += testMinRange;
			float output = evaluateFunction(input);
			float error = evaluateSingleTestCase(interpreter, ind, -1, input,
					output);
			if (error < hitThreshold) {
				hits++;
			}
		}
		float fitness;
		if (Float.isInfinite(errorSum)) {
			fitness = Float.MAX_VALUE;
		} else {
			fitness = errorSum / numOfTestPoints;
		}
		setFitness(state, thread, ind, fitness, hits);
	}

	/**
	 * Evaluate absolute error for given input value having individual and 
	 * expected output   
	 * @param interpreter
	 * @param individual
	 * @param testCaseNo
	 * @param input
	 * @param output
	 * @return
	 */
	public float evaluateSingleTestCase(Interpreter interpreter,
			SemanticIndividual individual, int testCaseNo, float input, float output) {
		interpreter.ClearStacks();
		// pushing input value to the float stack number of times (typically once)
		for (int i = 0; i < repeatFloatStack; i++) {
			interpreter.floatStack().push(input);
		}
		if (makeInputs) {
			// setting input value to input stack
			interpreter.inputStack().push((Float)input);
		}
		// executing the program
		interpreter.Execute(individual.program,
				interpreter.getExecutionLimit());
		// Penalize individual if there is no result on the stack.
		if (interpreter.floatStack().size() == 0) {
			return 1000.0f;
		}
		// compute result as absolute difference
		float error = Math.abs(interpreter.floatStack().top() - output);
		return error;
	}
	
	/**
	 * Function that we're searching through evolutionary process
	 * @param x argument of the function
	 * @return value for given argument
	 */
	abstract float evaluateFunction(float x);

}