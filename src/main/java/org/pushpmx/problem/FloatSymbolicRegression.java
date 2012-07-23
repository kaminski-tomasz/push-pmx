package org.pushpmx.problem;

import org.ecj.psh.PshEvolutionState;
import org.ecj.psh.PshIndividual;
import org.ecj.psh.PshProblem;
import org.spiderland.Psh.Interpreter;
import org.spiderland.Psh.InterpreterState;
import org.spiderland.Psh.Program;
import org.spiderland.Psh.SemanticInterpreter;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.koza.KozaFitness;
import ec.util.Parameter;

public abstract class FloatSymbolicRegression extends PshProblem {

	public static final String P_REPEATFLOATSTACK = "repeat-float-stack";

	public static final String P_PENALIZE = "penalize";
	public static final String P_INCLUDEHITS = "include-hits";
	public static final String P_HITSFACTOR = "hits-factor";
	
	public static final String P_TRAINMINRANGE = "train-min-range";
	public static final String P_TRAINMAXRANGE = "train-max-range";
	public static final String P_TRAINNUMPOINTS = "train-points";

	public static final String P_TESTMINRANGE = "test-min-range";
	public static final String P_TESTMAXRANGE = "test-max-range";
	public static final String P_TESTNUMPOINTS = "test-points";

	public static final String P_TESTPOINTSRES = "test-points-resolution";
	public static final String P_MAKEINPUTS = "make-inputs";			
	public static final String P_HITTHRESHOLD = "hit-threshold";
	
	/** Should we take into account hits while evaluating individual */
	public boolean includeHits;
	
	/** Should penalize individual when float stack is empty */
	public boolean penalizeIndividuals;
	
	/** Hits' coefficient multplied by hits value when evaluating */ 
	public float hitsFactor;
	
	/** How many times should input number be duplicated in float stack */
	public int repeatFloatStack;

	/** Lower range of the training set */
	public float trainMinRange;

	/** Upper range of the training set */
	public float trainMaxRange;

	/** Number of test cases used in evolution */
	public int numOfTrainPoints;

	/** Lower range of the testing set */
	public float testMinRange;

	/** Upper range of the testing set */
	public float testMaxRange;

	/** (Optional) resolution of testing points */
	public float testPointsResolution;
	
	/** Number of test cases used in testing the evolved solution */
	public int numOfTestPoints;

	/** Value under which individual hits the test case */
	public float hitThreshold;

	/** Should input value be pushed onto input stack? */
	public boolean makeInputs;
	
	/** Points generated to be the training set of test-cases */
	public double[][] trainPoints = null;

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
				base.push(P_TESTPOINTSRES), def.push(P_TESTPOINTSRES), 0.0f);
		
		hitThreshold = state.parameters.getFloatWithDefault(
				base.push(P_HITTHRESHOLD), def.push(P_HITTHRESHOLD), 0.01f);
		
		makeInputs = state.parameters.getBoolean(base.push(P_MAKEINPUTS),
				def.push(P_MAKEINPUTS), false);
		
		penalizeIndividuals = state.parameters.getBoolean(base.push(P_PENALIZE),
				def.push(P_PENALIZE), true);
		
		includeHits = state.parameters.getBoolean(base.push(P_INCLUDEHITS),
				def.push(P_INCLUDEHITS), false);
		
		hitsFactor = state.parameters.getFloatWithDefault(
				base.push(P_HITSFACTOR), def.push(P_HITSFACTOR), 10.f);
		
		// Generating the training set
		state.output.message("Training set test cases: ");
		trainPoints = new double[numOfTrainPoints][];
		for (int i = 0; i < numOfTrainPoints; i++) {
			trainPoints[i] = new double[2];
			trainPoints[i][0] = trainMinRange + i
					* (trainMaxRange - trainMinRange) / (numOfTrainPoints - 1);
			trainPoints[i][1] = evaluateFunction(trainPoints[i][0]);
			state.output.message("[ " + trainPoints[i][0] + ", "
					+ trainPoints[i][1] + " ]");
		}
	}
	
	@Override
	public void evaluate(EvolutionState state, Individual ind,
			int subpopulation, int threadnum) {
		if (ind.evaluated)
			return;

		if (!(ind instanceof PshIndividual)) {
			state.output.fatal("This is not PshIndividual instance!");
		}
		Interpreter interpreter = ((PshEvolutionState) state).interpreter[threadnum];
		evaluateTrainSet(state, threadnum, interpreter,
				(PshIndividual) ind);
	}

	/**
	 * Set the fitness and hits value for the individual
	 * 
	 * @param ind
	 * @param fitness
	 * @param hits
	 */
	public void setFitness(EvolutionState state, int thread,
			PshIndividual ind, float fitness, int hits) {
		if (includeHits) {
			fitness += hitsFactor * (1.0f - hits / (float) numOfTrainPoints);
		}
		KozaFitness f = (KozaFitness) ind.fitness;
		f.setStandardizedFitness(state, fitness);
		f.hits = hits;
		ind.evaluated = true;
	}
	
	/**
	 * Evaluate the individual for training set points
	 * 
	 * @param interpreter
	 * @param ind
	 */
	public void evaluateTrainSet(EvolutionState state, int thread,
			Interpreter interpreter, PshIndividual ind) {
		double errorSum = 0.0;
		int hits = 0;
		for (int i = 0; i < numOfTrainPoints; i++) {
			double input = trainPoints[i][0];
			double output = trainPoints[i][1];
			double error = evaluateSingleTestCase(interpreter, ind, i, input,
					output);
			if (error <= hitThreshold) {
				hits++;
			}
			errorSum += error;
		}
		float fitness;
		if (Double.isInfinite(errorSum)) {
			fitness = Float.MAX_VALUE;
		} else {
			fitness = (float)(errorSum / numOfTrainPoints);
		}
		setFitness(state, thread, ind, fitness, hits);
	}
	
	/**
	 * Evaluate the individual for test set points chosen randomly
	 * @param interpreter
	 * @param ind
	 */
	public void evaluateTestSet(EvolutionState state, int thread,
			Interpreter interpreter, PshIndividual ind) {
		double errorSum = 0.0;
		int hits = 0;
		for (int i = 0; i < numOfTestPoints; i++) {
			// generate random point in given range
			double input = state.random[thread].nextDouble()
					* (testMaxRange - testMinRange);
			if (testPointsResolution > 0.0f) {
				input -= input % testPointsResolution;
			}
			input += testMinRange;
			double output = evaluateFunction(input);
			double error = evaluateSingleTestCase(interpreter, ind, -1, input,
					output);
			if (error <= hitThreshold) {
				hits++;
			}
			errorSum += error;
		}
		float fitness;
		if (Double.isInfinite(errorSum)) {
			fitness = Float.MAX_VALUE;
		} else {
			fitness = (float)(errorSum / numOfTestPoints);
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
	public double evaluateSingleTestCase(Interpreter interpreter,
			PshIndividual individual, int testCaseNo, double input, double output) {
		interpreter.ClearStacks();
		// pushing input value to the float stack number of times (typically once)
		for (int i = 0; i < repeatFloatStack; i++) {
			interpreter.floatStack().push((float)input);
		}
		if (makeInputs) {
			// setting input value to input stack
			interpreter.inputStack().push((Float)((float)input));
		}
		// executing the program
		interpreter.Execute(individual.program,
				interpreter.getExecutionLimit());
		// Penalize individual if there is no result on the stack.
		if (penalizeIndividuals && interpreter.floatStack().size() == 0) {
			return 1000.0f;
		}
		// compute result as absolute difference
		double error = Math.abs(interpreter.floatStack().top() - output);
		return error;
	}
	
	/**
	 * Create initial interpreter memory state for each test case
	 * 
	 * @return array of interpreter states
	 */
	public InterpreterState[] initInterpreterStateArray() {
		InterpreterState[] states = new InterpreterState[numOfTrainPoints];
		for (int i = 0; i < numOfTrainPoints; i++) {
			states[i] = new InterpreterState();
			// get training point
			float input = (float)trainPoints[i][0];
			// pushing input value to the float stack number of times (typically
			// once)
			for (int k = 0; k < repeatFloatStack; k++) {
				states[i].getFloatStack().push(input);
			}
			if (makeInputs) {
				// setting input value to input stack
				states[i].getInputStack().push((Float) input);
			}
		}
		return states;
	}

	/**
	 * Compute interpreter memory state having given initial state for each test
	 * case
	 * 
	 * @param initStates
	 *            array of initial states for each test case
	 * @param interpreter
	 * @param program
	 * @return
	 */
	public InterpreterState[] computeInterpreterStateArray(
			InterpreterState[] initStates, SemanticInterpreter interpreter,
			Program program) {
		if (initStates.length != numOfTrainPoints)
			throw new InternalError();
		InterpreterState[] states = new InterpreterState[numOfTrainPoints];
		for (int i = 0; i < numOfTrainPoints; i++) {
			InterpreterState state = initStates[i].clone();
			interpreter.setMemoryState(state);
			interpreter.Execute(program);
			states[i] = interpreter.getMemoryState().clone();
		}
		return states;
	}
	
	@Override
	public void describe(EvolutionState state, Individual ind,
			int subpopulation, int threadnum, int log) {

		Interpreter interpreter = ((PshEvolutionState) state).interpreter[threadnum];
		
		for (int i = 0; i < numOfTrainPoints; i++) {
			double input = trainPoints[i][0];
			double output = trainPoints[i][1];
			double answer = 0.0;
			interpreter.ClearStacks();
			// pushing input value to the float stack number of times (typically once)
			for (int k = 0; k < repeatFloatStack; k++) {
				interpreter.floatStack().push((float)input);
			}
			if (makeInputs) {
				// setting input value to input stack
				interpreter.inputStack().push((Float)((float)input));
			}
			// executing the program
			interpreter.Execute(((PshIndividual)ind).program,
					interpreter.getExecutionLimit());

			if (interpreter.floatStack().size() == 0) {
				answer = 0.0;
			} else {
				answer = interpreter.floatStack().top();
			}
			state.output.println("" + input + " " + output + " " + answer, log);
		}
	}

	/**
	 * Function that we're searching through evolutionary process
	 * @param x argument of the function
	 * @return value for given argument
	 */
	protected abstract double evaluateFunction(double x);

}
