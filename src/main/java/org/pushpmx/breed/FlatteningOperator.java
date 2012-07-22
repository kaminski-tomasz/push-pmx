package org.pushpmx.breed;

import org.ecj.psh.PshDefaults;
import org.ecj.psh.PshIndividual;
import org.ecj.psh.breed.MutationPipeline;

import ec.EvolutionState;
import ec.util.Parameter;

public class FlatteningOperator extends MutationPipeline {

	public static final String P_FLATMUTATE = "flatten";

	@Override
	public Parameter defaultBase() {
		return PshDefaults.base().push(P_FLATMUTATE);
	}

	@Override
	protected void mutate(PshIndividual ind, EvolutionState state, int thread,
			int subpopulation) {
		while (ind.program.size() == 1 && ind.program.programsize() > 1) {
			ind.program.Flatten(0);
		}
	}

}
