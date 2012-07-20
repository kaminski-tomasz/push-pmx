package org.pushpmx.breed;

import java.util.ArrayList;

import org.ecj.psh.PshEvolutionState;
import org.ecj.psh.breed.CrossoverPipeline;
import org.ecj.psh.breed.PshBreedDefaults;
import org.pushpmx.Semantics;
import org.pushpmx.metric.SemanticsMetric;
import org.pushpmx.problem.FloatSymbolicRegression;
import org.pushpmx.util.Permutation;
import org.spiderland.Psh.InterpreterState;
import org.spiderland.Psh.Program;
import org.spiderland.Psh.SemanticInterpreter;

import ec.EvolutionState;
import ec.Individual;
import ec.util.Parameter;

public class SemanticCrossover extends CrossoverPipeline {

	public static final String P_PMXOVER = "pmx-xover";
	
	public static final String P_SUBPROGRAMLEN = "subprogram-length";
	public static final String P_SEARCHINGSTEPS = "searching-steps";
	public static final String P_METRIC = "metric";
	public static final String P_GREEDY = "greedy-search";
	public static final String P_SHUFFLE = "shuffle";
		
	/** Length of the exchanging subprograms */
	public int replacementLength;

	/** Set of the subprograms to be tested */
	public ArrayList<Program> subprogramSpace;
	
	/** Should subprograms subspace be shuffled before use */
	public boolean shuffleSubprograms;
	
	/**
	 * Number of searching steps (must be less or equal to the subprograms set
	 * size)
	 */
	public int searchingSteps;

	/**
	 * Metric used to compute distance between programs behavior in semantic
	 * space
	 */
	public SemanticsMetric metric;
	
	/**
	 * Should be applied greedy approach when searching in subprograms space
	 */
	public boolean greedySearch;
	
	/** Permutation of subprogram indices */
	public Permutation permutation;	

	
	@Override
	public Parameter defaultBase() {
		return PshBreedDefaults.base().push(P_PMXOVER);
	}

	@Override
	public SemanticCrossover clone() {
		SemanticCrossover sc = (SemanticCrossover)super.clone();
		sc.permutation = permutation.clone();
		return sc;
	}
	
	@Override
	public void setup(EvolutionState state, Parameter base) {
		super.setup(state, base);
		Parameter def = defaultBase();

		// Length of the replacement
		replacementLength = state.parameters.getIntWithDefault(
				base.push(P_SUBPROGRAMLEN), def.push(P_SUBPROGRAMLEN), 1);

		// generate subprograms (we don't involve any RNG here)
		subprogramSpace = ((SemanticInterpreter) ((PshEvolutionState) state).interpreter[0])
				.generateAllPrograms(replacementLength);
		
		// The number of steps for searching the space of subprograms
		searchingSteps = state.parameters.getIntWithDefault(
				base.push(P_SEARCHINGSTEPS), def.push(P_SEARCHINGSTEPS), 100);
		
		// distance metric of the semantics space
		Parameter p = base.push(P_METRIC);
		Parameter d = def.push(P_METRIC);
		metric = (SemanticsMetric) (state.parameters.getInstanceForParameter(p,
				d, SemanticsMetric.class));
		metric.setup(state, p);
				
		// should we shuffle the space of subprograms
		shuffleSubprograms = state.parameters.getBoolean(base.push(P_SHUFFLE),
				def.push(P_SHUFFLE), true);		
		
		permutation = new Permutation(subprogramSpace.size());		
		
		// should we stop searching when found the improvement of the distance measure
		greedySearch = state.parameters.getBoolean(base.push(P_GREEDY),
				def.push(P_GREEDY), false);
	}

	@Override
	public int produce(int min, int max, int start, int subpopulation,
			Individual[] inds, EvolutionState state, int thread) {

		problem = (FloatSymbolicRegression) state.evaluator.p_problem.clone();

		interpreter = (SemanticInterpreter)((PshEvolutionState) state).interpreter[thread];
		
		return super.produce(min, max, start, subpopulation, inds, state,
				thread);
	}

	@Override
	protected void crossover(EvolutionState state, int thread,
			boolean breedSecondParent) {

		// parents size
		int p1size = (int) parents[0].program.size();
		int p2size = (int) parents[1].program.size();

		if (p1size <= replacementLength || p2size <= replacementLength)
			return;

		// generate cutting points drawn from random
		int p1cutpoint = 0, p2cutpoint = 0;
		if (!homologous) {
			p1cutpoint = generateCuttingPoint(state, thread, p1size);
			p2cutpoint = generateCuttingPoint(state, thread, p2size);
		} else {
			p1cutpoint = p2cutpoint = generateCuttingPoint(state, thread,
					Math.min(p1size, p2size));
		}

		// prepare required subprograms of parents
		Program[] p1parts = decomposeProgram(parents[0].program, p1size, p1cutpoint);
		Program[] p2parts = decomposeProgram(parents[1].program, p2size, p2cutpoint);

		// determine memory states of interpreter for each traininig points
		// after executing the prefix of the parent program
		InterpreterState[] p1MemoryStateArray = problem
				.computeInterpreterStateArray(
						problem.initInterpreterStateArray(), interpreter,
						p1parts[0]);
		InterpreterState[] p2MemoryStateArray = problem
				.computeInterpreterStateArray(
						problem.initInterpreterStateArray(), interpreter,
						p2parts[0]);

		// determine the semantics of the parents at the cutpoint +
		// replacementLength location
		Semantics p1Semantics = computeSemantics(p1MemoryStateArray, p1parts[1]);
		Semantics p2Semantics = computeSemantics(p2MemoryStateArray, p2parts[1]);

		if (shuffleSubprograms) {
			permutation.shuffle(state, thread, searchingSteps);
		}
		
		int index = permutation.get(0);
		Program replacement = subprogramSpace.get(index);
		Program bestReplacement = replacement;

		// determine the semantics of the offspring with the given subprogram
		Semantics ofSemantics = computeSemantics(p2MemoryStateArray, replacement);

		float divergence = computeDivergence(p1Semantics, p2Semantics,
				ofSemantics);

		// main loop
		for (int step = 1; divergence > 0.0f && step < searchingSteps
				&& step < subprogramSpace.size(); step++) {
			
			index = permutation.get(step);
			replacement = subprogramSpace.get(index);

			// determine the semantics of the offspring with the given
			// subprogram
			InterpreterState[] which = (step % 2 == 0) ? p1MemoryStateArray
					: p2MemoryStateArray;
			ofSemantics = computeSemantics(which, replacement);

			// compute divergenvce
			float newDivergence = computeDivergence(p1Semantics, p2Semantics,
					ofSemantics);

			if (newDivergence < divergence) {
				divergence = newDivergence;
				bestReplacement = replacement;
				if (greedySearch)
					break;
			}

		}
		if (Float.isInfinite(divergence))
			return;
		if (Float.isNaN(divergence))
			return;
				
		
		p1parts[1] = bestReplacement; // replace the middle part
		joinDecomposedProgram(p1parts, parents[0].program);
		parents[0].evaluated = false;
		
		if (breedSecondParent) {
			p2parts[1] = bestReplacement; // replace the middle part
			joinDecomposedProgram(p2parts, parents[1].program);
			parents[1].evaluated = false;
		}
		
	}

	/**
	 * Decompose program into three parts: prefix part, replaced part and suffix part.
	 * @param program Push program to be decomposed
	 * @param programSize program size
	 * @param cutpoint cutting point
	 * 
	 * @return
	 */
	public Program[] decomposeProgram(Program program, int programSize,
			int cutpoint) {
		Program[] parts = new Program[3];
		// prepare required subprograms of parents
		parts[0] = program.Copy(0, cutpoint); 					// prefix part
		parts[1] = program.Copy(cutpoint, replacementLength); 	// replaced part
		parts[2] = program.Copy(cutpoint + replacementLength, 	// suffix part
				programSize - (cutpoint + replacementLength));
		return parts;
	}

	/**
	 * Joins decomposed program into one chunk 
	 * @param parts
	 * @param result
	 */
	public void joinDecomposedProgram(Program[] parts, Program result){
		result.clear();
		for (int i = 0; i < parts.length; i++) {
			parts[i].CopyTo(result);
		}
	}
	
	/**
	 * Generates the starting point for replaced part
	 * 
	 * @param programSize
	 *            program size
	 * @return
	 */
	public int generateCuttingPoint(EvolutionState state, int thread,
			int programSize) {
		int cuttingPoint = state.random[thread].nextInt(programSize
				- replacementLength + 1);
		return cuttingPoint;
	}
	
	/**
	 * Computes semantics for given program and initial interpreter state  for
	 * each test-case coming from training set
	 * 
	 * @param initialStateArray
	 *            initial interpreter states
	 * @param program
	 *            program
	 * @return
	 */
	public Semantics computeSemantics(InterpreterState[] initialStateArray,
			Program program) {
		InterpreterState[] stateArray = problem.computeInterpreterStateArray(
				initialStateArray, interpreter, program);
		Semantics semantics = new Semantics(stateArray);
		return semantics;
	}
	
	// abstract public float computeDivergence(Semantics p1, Semantics p2,
	// Semantics o);
	// TODO
	public float computeDivergence(Semantics p1, Semantics p2, Semantics o) {
		
		// divergence from equidistance
		float distance_p1_o = metric.getDistance(p1, o);
		float distance_p2_o = metric.getDistance(p2, o);
		float result = Math.abs(distance_p1_o - distance_p2_o);
		return result;

		// divergence from geometricity
//		 return metric.getDistance(p1, o) + metric.getDistance(p2, o) -
//		 metric.getDistance(p1, p2);
	}

	/** temporary helper fields */
	private FloatSymbolicRegression problem = null;
	private SemanticInterpreter interpreter = null;
}
