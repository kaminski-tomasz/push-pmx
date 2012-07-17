package org.pushpmx.breed;

import org.ecj.psh.PshProblem;
import org.ecj.psh.breed.CrossoverPipeline;
import org.ecj.psh.breed.PshBreedDefaults;

import ec.EvolutionState;
import ec.Individual;
import ec.util.Parameter;

public class PmxCrossover extends CrossoverPipeline {

	public static final String P_PMXOVER = "pmx-xover";
	public static final String P_SUBPROGRAMLEN = "subprogram-length";
	
	/** Length of the subprograms */
	public int subprogramLength;
	
	private PshProblem problem;
	
	@Override
	public Parameter defaultBase() {
		return PshBreedDefaults.base().push(P_PMXOVER);
	}

	@Override
	public void setup(EvolutionState state, Parameter base) {
		super.setup(state, base);
		Parameter def = this.defaultBase();
		
		// Length of the subprograms
		subprogramLength = state.parameters.getIntWithDefault(
				base.push(P_SUBPROGRAMLEN), def.push(P_SUBPROGRAMLEN), 1);
		
		
		
	}
	
	

	@Override
	public int produce(int min, int max, int start, int subpopulation,
			Individual[] inds, EvolutionState state, int thread) {
		
		problem = (PshProblem)state.evaluator.p_problem.clone();
		
		return super.produce(min, max, start, subpopulation, inds, state, thread);
	}

	@Override
	protected void crossover(EvolutionState state, int thread,
			boolean breedSecondParent) {
	
		if (parents[0].size() <= 0 || parents[0].size() <= 0)
			return;
		int parent1size = (int) parents[0].size();
		int cutpoint1 = state.random[thread].nextInt(parent1size);
		
//		problem.
		
	}
	
	
	
	

}
