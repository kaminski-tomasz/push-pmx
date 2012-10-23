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

package org.pushpmx.metric;

import org.ecj.psh.PshDefaults;

import ec.EvolutionState;
import ec.util.Parameter;

public class TopMetric extends SemanticsMetric {

	public static final String P_METRIC = "top-metric";
	public static final String P_TYPE = "type";

	public static final String V_HAMMING = "hamming";
	public static final String V_CITYBLOCK = "city-block"; 
	
	public static final int C_HAMMING = 0;
	public static final int C_CITYBLOCK = 1;

	/** peek metric type */
	public int type;

	@Override
	public Parameter defaultBase() {
		return PshDefaults.base().push(P_METRIC);
	}

	@Override
	public void setup(EvolutionState state, Parameter base) {
		super.setup(state, base);
		Parameter def = defaultBase();

		String typeStr = state.parameters.getString(base.push(P_TYPE),
				def.push(P_TYPE));
		type = C_HAMMING;
		if (V_HAMMING.equals(typeStr)) {
			type = C_HAMMING;
		} else if (V_CITYBLOCK.equals(typeStr)) {
			type = C_CITYBLOCK;
		} else if (typeStr != null) {
			state.output.fatal("Unknown top metric type: " + typeStr);
		}
	}

	@Override
	protected double getStackDistance(float[] stack1, float[] stack2) {
		if (stack1 == null || stack2 == null)
			throw new InternalError();
		if (stack1.length == 0 && stack2.length == 0)
			return 0.0;
		if (stack1.length == 0 || stack2.length == 0) {
			switch (type) {
			case C_HAMMING:
				return 1.0;
			case C_CITYBLOCK:
				return Double.POSITIVE_INFINITY;
			}
		}
		float peek1 = stack1[stack1.length - 1];
		float peek2 = stack2[stack2.length - 1];
		switch (type) {
		case C_CITYBLOCK:
			return Math.abs(peek1 - peek2);
		default:
		case C_HAMMING:
			if (Math.abs(peek1 - peek2) > epsilon)
				return 1.0;
			return 0.0;
		}
	}

}
