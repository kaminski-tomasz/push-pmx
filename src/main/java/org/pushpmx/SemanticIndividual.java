package org.pushpmx;

import org.ecj.psh.PshIndividual;
import org.spiderland.Psh.ObjectStack;

import ec.EvolutionState;
import ec.util.Parameter;

/**
 * Push Individual with semantics saved in trace field
 * @author tomek
 *
 */
public class SemanticIndividual extends PshIndividual {

	/** Stack trace evaluated in Problem for each testcase */
	public ObjectStack[] trace;

	@Override
	public SemanticIndividual clone() {
		SemanticIndividual ind = (SemanticIndividual) super.clone();
		// stack trace can be shared between cloned individuals
		return ind;
	}
	
	@Override
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);
		// traces are set in Evaluator
		trace = null;
	}
	
	/**
	 * Used in printIndividual methods
	 */
	@Override
	public String toString() {
		String result = this.program.toString();
		
		/*
		if (this.trace != null)
			for (ObjectStack testCaseResults : trace)
				result += "\n\t" + testCaseResults;
		*/
		return result;
	}
}
