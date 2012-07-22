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

public class PartiallyMedialCrossover extends CrossoverPipeline {

	public static final String P_PMXOVER = "pmx-xover";
	
	public static final String P_SUBPROGRAMLEN = "replacement-length";
	public static final String P_SEARCHINGSTEPS = "steps";
	public static final String P_METRIC = "metric";
	public static final String P_GREEDY = "greedy";
	public static final String P_SHUFFLE = "shuffle";
	public static final String P_DIVERGENCETYPE = "divergence";
	
	public static final int V_EQUIDISTANCE = 0;
	public static final int V_GEOMETRICITY = 1;
	
	/** Type of the divergence to minimize */
	public int divergenceType;
	
	/** Length of the exchanging subprograms */
	public int replacementLength;

	/** Set of the subprograms to be tested */
	public ArrayList<Program> subprogramSpace;
	
	/** Buffer array for a size of the replacing programs */
	public int[] sizeBuffer;
	
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
	 * Should greedy approach be applied when searching the subprograms space
	 */
	public boolean greedySearch;
	
	/** Permutation of subprogram indices */
	public Permutation permutation;	

	
	@Override
	public Parameter defaultBase() {
		return PshBreedDefaults.base().push(P_PMXOVER);
	}

	@Override
	public PartiallyMedialCrossover clone() {
		PartiallyMedialCrossover sc = (PartiallyMedialCrossover) super.clone();
		// we need to clone deeply only the permutation
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

		// generate subprograms (we don't involve any RNG here). Moreover,
		// generated programs contain String names of instructions, not
		// references to objects which belong to the interpreter
		subprogramSpace = ((SemanticInterpreter) ((PshEvolutionState) state).interpreter[0])
				.generateAllPrograms(replacementLength);
		
		// create the space of replacing subprograms 
		sizeBuffer = new int[subprogramSpace.size()];
		for (int i = 0; i < subprogramSpace.size(); i++) {
			sizeBuffer[i] = subprogramSpace.get(i).programsize(); 
		}
		
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
		
		// should we stop searching when we find the improvement of the divergence
		greedySearch = state.parameters.getBoolean(base.push(P_GREEDY),
				def.push(P_GREEDY), false);
		
		// type of the divergence to minimize
		p = base.push(P_DIVERGENCETYPE);
		d = def.push(P_DIVERGENCETYPE);
		String divergenceTypeString = state.parameters.getString(p, d);
		if ("equidistance".equals(divergenceTypeString)) {
			divergenceType = V_EQUIDISTANCE;
		} else if ("geometricity".equals(divergenceTypeString)) {
			divergenceType = V_GEOMETRICITY;
		} else {
			state.output.fatal("Unknown value " + divergenceTypeString, p, d);
		}
	}

	@Override
	public int produce(int min, int max, int start, int subpopulation,
			Individual[] inds, EvolutionState state, int thread) {

		problem = (FloatSymbolicRegression) state.evaluator.p_problem.clone();
		interpreter = (SemanticInterpreter) ((PshEvolutionState) state).interpreter[thread];

		return super.produce(min, max, start, subpopulation, inds, state,
				thread);
	}

	@Override
	protected void crossover(EvolutionState state, int thread,
			boolean breedSecondParent) {

		Program p1 = parents[0].program;
		Program p2 = parents[1].program;

		// parents length (precisely, sizes of their root stacks)
		int p1Length = p1.size();
		int p2Length = p2.size();

		if (p1Length <= 0 || p2Length <= 0 || p1Length < replacementLength
				|| p2Length < replacementLength)
			return;

		// generate cutting points drawn from random
		int p1cutpoint = 0, p2cutpoint = 0;
		if (!homologous) {
			p1cutpoint = findCuttingPoint(state, thread, p1Length);
			p2cutpoint = findCuttingPoint(state, thread, p2Length);
		} else {
			p1cutpoint = p2cutpoint = findCuttingPoint(state, thread,
					Math.min(p1Length, p2Length));
		}

		// prepare required subprograms of parents
		Program[] p1parts = decomposeProgram(p1, p1Length, p1cutpoint);
		Program[] p2parts = decomposeProgram(p2, p2Length, p2cutpoint);

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
		distance_p1_p2 = metric.getDistance(p1Semantics, p2Semantics);
		
		if (divergenceType == V_GEOMETRICITY && Double.isInfinite(distance_p1_p2)) {
			// we can't find semantically medial offspring for programs
			// that are infinitely distant from each other in semantic space 
			return;
		}
		
		if (shuffleSubprograms) {
			permutation.shuffle(state, thread, searchingSteps);
		}
		
		int p1Size = p1.programsize();
		int p2Size = p2.programsize();
		int p1ReplacedSize = p1parts[1].programsize();
		int p2ReplacedSize = p2parts[1].programsize();
		int maxPointsInProgram = interpreter.getMaxPointsInProgram();
		
		Program replacement = null;
		Program bestReplacement = replacement;
		double divergence = Double.POSITIVE_INFINITY;
		double bestDivergence = divergence;
		int step = 0;
		Semantics ofSemantics = null;
		do {
			// get the replacing program and its size (size, not length of a root stack)
			int index = permutation.get(step);
			replacement = subprogramSpace.get(index);
			int replacementSize = sizeBuffer[index];

			// determine the interpreter memory state to be used and check whether size of the offspring is correct
			InterpreterState[] whichMemoryState;
			boolean isSizeCorrect;

			if (breedSecondParent) {
				whichMemoryState = (step % 2 == 0) ? p1MemoryStateArray
						: p2MemoryStateArray;
				isSizeCorrect = ((p1Size - p1ReplacedSize + replacementSize) <= maxPointsInProgram)
						&& ((p2Size - p2ReplacedSize + replacementSize) <= maxPointsInProgram);
			} else {
				whichMemoryState = p1MemoryStateArray;
				isSizeCorrect = ((p1Size - p1ReplacedSize + replacementSize) <= maxPointsInProgram);
			}

			// if size is too big, check the next replacing subprogram
			if (!isSizeCorrect)
				continue;

			// determine the semantics of the offspring with the given subprogram
			ofSemantics = computeSemantics(whichMemoryState,
					replacement);

			// compute divergence
			divergence = computeDivergence(p1Semantics, p2Semantics,
					ofSemantics);
			if (divergence < 0.0) {
				// fix rounding errors
				divergence = 0.0;
			}
			// check if we found improvement
			if (bestReplacement != null) {
				if (divergence < bestDivergence) {
					bestReplacement = replacement;
					bestDivergence = divergence;
					if (greedySearch)
						break;
				}
			} else {
				bestReplacement = replacement;
				bestDivergence = divergence;
			}
			++ step;
		} while (step < searchingSteps && step < subprogramSpace.size()
				&& divergence > metric.epsilon);
		
		if (bestReplacement == null || Double.isInfinite(bestDivergence))
			return;
		if (Double.isNaN(bestDivergence))
			throw new InternalError("Best divergence is NaN");		// should not happen

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
	public int findCuttingPoint(EvolutionState state, int thread,
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
	
	/**
	 * Computes divergence from equidistance or geometricity
	 * 
	 * @param p1
	 *            semantics of parent1
	 * @param p2
	 *            semantics of parent2
	 * @param o
	 *            semantics of offspring
	 * @return divergence from chosen measure
	 */
	public double computeDivergence(Semantics p1, Semantics p2, Semantics o) {
		// compute required distances
		double distance_p1_o = metric.getDistance(p1, o);
		if (Double.isInfinite(distance_p1_o)) {
			return Double.POSITIVE_INFINITY;
		}
		double distance_p2_o = metric.getDistance(p2, o);
		if (Double.isInfinite(distance_p2_o)) {
			return Double.POSITIVE_INFINITY;
		}
		switch (divergenceType) {
		case V_GEOMETRICITY:
			// compute divergence from geometricity
			return distance_p1_o + distance_p2_o - distance_p1_p2;
		default:
		case V_EQUIDISTANCE:
			// compute divergence from equidistance
			return Math.abs(distance_p1_o - distance_p2_o);
		}
	}

	/** temporary helper fields */
	private FloatSymbolicRegression problem = null;
	private SemanticInterpreter interpreter = null;
	private double distance_p1_p2;

}
