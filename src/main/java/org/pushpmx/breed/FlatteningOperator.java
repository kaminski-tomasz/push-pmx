/*
* Copyright 2012 Tomasz Kamiński
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

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
