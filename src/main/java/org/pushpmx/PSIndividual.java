package org.pushpmx;

import org.ecj.psh.PshIndividual;

import ec.EvolutionState;
import ec.util.Parameter;

/**
 * Push Individual with semantics 
 * @author tomek
 *
 */
public class PSIndividual extends PshIndividual {

	/** Semantics evaluated in Problem */
	public Semantics semantics;

	@Override
	public PSIndividual clone() {
		PSIndividual ind = (PSIndividual) super.clone();
		ind.semantics = this.semantics.clone();
		return ind;
	}
	
	@Override
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);
		semantics = new Semantics();
	}
}
