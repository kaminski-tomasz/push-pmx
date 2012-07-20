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

public class PmxCrossover extends CrossoverPipeline {

	public static final String P_PMXOVER = "pmx-xover";
	
	public static final String P_SUBPROGRAMLEN = "subprogram-length";
	public static final String P_SEARCHINGSTEPS = "searching-steps";
	public static final String P_METRIC = "metric";
	public static final String P_GREEDY = "greedy";
	public static final String P_SHUFFLE = "shuffle";
		
	/** Length of the exchanging subprograms */
	public int replacementLength;

	/** Set of the subprograms to be tested */
	public ArrayList<Program> subprogramSet;
	
	/** Should subprograms subspace be shuffled before use */
	public boolean shuffleSubprograms;
	
	/**
	 * Number of searching steps (must be less or equal to the subprograms set
	 * size
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
			
	private Permutation perm;
	
	@Override
	public Parameter defaultBase() {
		return PshBreedDefaults.base().push(P_PMXOVER);
	}

	@Override
	public void setup(EvolutionState state, Parameter base) {
		super.setup(state, base);
		Parameter def = defaultBase();

		// Length of the replacement
		replacementLength = state.parameters.getIntWithDefault(
				base.push(P_SUBPROGRAMLEN), def.push(P_SUBPROGRAMLEN), 1);

		// generate subprograms (we don't involve any RNG here)
		subprogramSet = ((SemanticInterpreter) ((PshEvolutionState) state).interpreter[0])
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
		
		if (shuffleSubprograms) {
			perm = new Permutation(subprogramSet.size());
		}
		
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
		int parent1size = (int) parents[0].program.size();
		int parent2size = (int) parents[1].program.size();

		if (parent1size <= replacementLength || parent2size <= replacementLength)
			return;

		// generate random cutting points
		int p1cutpoint = 0, p2cutpoint = 0;
		if (!homologous) {
			p1cutpoint = state.random[thread].nextInt(parent1size
					- replacementLength + 1);
			p2cutpoint = state.random[thread].nextInt(parent2size
					- replacementLength + 1);
		} else {
			p1cutpoint = p2cutpoint = state.random[thread].nextInt(Math.min(
					parent1size, parent2size) - replacementLength + 1);
		}

		// prepare required subprograms of parents
		Program p1prefix = parents[0].program.Copy(0, p1cutpoint);
		Program p1exchange = parents[0].program
				.Copy(p1cutpoint, replacementLength);
		Program p1suffix = parents[0].program.Copy(p1cutpoint + replacementLength,
				parent1size - (p1cutpoint + replacementLength));

		Program p2prefix = parents[1].program.Copy(0, p2cutpoint);
		Program p2exchange = parents[1].program
				.Copy(p2cutpoint, replacementLength);
		Program p2suffix = parents[1].program.Copy(p2cutpoint + replacementLength,
				parent2size - (p2cutpoint + replacementLength));

		// determine memory states of interpreter for each traininig points
		// after executing the prefix of the parent program
		InterpreterState[] p1PrefixStates = problem.computeInterpreterStates(
				problem.initInterpreterStates(), interpreter, p1prefix);
		InterpreterState[] p2PrefixStates = problem.computeInterpreterStates(
				problem.initInterpreterStates(), interpreter, p2prefix);

		// determine the semantics of the parents after executing their exchange
		// subprograms
		InterpreterState[] p1states = problem.computeInterpreterStates(
				p1PrefixStates, interpreter, p1exchange);
		Semantics p1semantics = new Semantics(p1states);
		InterpreterState[] p2states = problem.computeInterpreterStates(
				p2PrefixStates, interpreter, p2exchange);
		Semantics p2semantics = new Semantics(p2states);

		
		if (shuffleSubprograms) {
			perm.shuffle(state, thread, searchingSteps);
		}
		
		int index = shuffleSubprograms? perm.get(0): 0;
		Program exchange = subprogramSet.get(index);
		Program bestExchange = exchange;
		
		// determine the semantics of the offspring with the given subprogram
		InterpreterState[] currentStates = problem.computeInterpreterStates(
				p2PrefixStates, interpreter, exchange);
		Semantics ofSemantics = new Semantics(currentStates);
		
		float divergence = computeDivergence(p1semantics, p2semantics, ofSemantics);

		for (int step = 1; divergence > 0.0 && step < searchingSteps && step < subprogramSet.size(); step++) {
			index = shuffleSubprograms ? perm.get(step) : step;
			exchange = subprogramSet.get(index);

			// determine the semantics of the offspring with the given
			// subprogram
			InterpreterState[] whichStates = step % 2 == 0 ? p1PrefixStates
					: p2PrefixStates;
			currentStates = problem.computeInterpreterStates(whichStates,
					interpreter, exchange);
			ofSemantics = new Semantics(currentStates);

			// compute divergenvce
			float newDivergence = computeDivergence(p1semantics, p2semantics,
					ofSemantics);

			if (newDivergence < divergence) {
				divergence = newDivergence;
				bestExchange = exchange;
				if (greedySearch)
					break;
			}

		}
		if (Float.isInfinite(divergence))
			return;
		if (Float.isNaN(divergence))
			return;
				
		bestExchange.CopyTo(p1prefix);
		p1suffix.CopyTo(p1prefix);
		parents[0].program = p1prefix;
		parents[0].evaluated = false;
		
		if (breedSecondParent) {
			bestExchange.CopyTo(p2prefix);
			p2suffix.CopyTo(p2prefix);
			parents[1].program = p2prefix;
			parents[1].evaluated = false;
		}
		
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
		// return metric.getDistance(p1, o) + metric.getDistance(p2, o) -
		// metric.getDistance(p1, p2);
	}

	/** temporary helper fields */
	private FloatSymbolicRegression problem;
	private SemanticInterpreter interpreter;	
	
}
