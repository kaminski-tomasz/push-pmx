package org.pushpmx.util;

import ec.EvolutionState;

public class Permutation implements Cloneable {

	int[] perm;

	@Override
	public Permutation clone() {
		try {
			Permutation p = (Permutation) super.clone();
			p.perm = this.perm.clone();
			return p;
		} catch (CloneNotSupportedException e) {
			throw new InternalError(); // never happens
		}
	}
	
	public Permutation(int numOfElements) {
		perm = new int[numOfElements];
		for (int i = 0; i < perm.length; i++) {
			perm[i] = i;
		}
	}

	public void shuffle(EvolutionState state, int thread, int n) {
		if (n <= 0) 
			n = perm.length;
		if (perm.length > 0)
			for (int i = 0; i < n && i < perm.length; i++) {
				int j = i + state.random[thread].nextInt(perm.length - i);
				if (j != i) {
					int swp = perm[i];
					perm[i] = perm[j];
					perm[j] = swp;
				}
			}
	}

	public int get(int i) {
		return perm[i];
	}

}
