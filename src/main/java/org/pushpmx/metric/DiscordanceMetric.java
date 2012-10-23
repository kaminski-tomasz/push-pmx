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

public class DiscordanceMetric extends SemanticsMetric {

	public static final String P_METRIC = "discordance-metric";
	public static final String P_COMPARETAILS = "compare-tails";
	
	/** Should we check tails of the vectors instead of heads */ 
	public boolean compareTails;
	
	@Override
	public Parameter defaultBase() {
		return PshDefaults.base().push(P_METRIC);
	}	
	
	@Override
	public void setup(EvolutionState state, Parameter base) {		 
		super.setup(state, base);
		Parameter def = defaultBase();
		compareTails = state.parameters.getBoolean(base.push(P_COMPARETAILS),
				def.push(P_COMPARETAILS), false);
	}
	
	@Override
	protected double getStackDistance(float[] stack1, float[] stack2) {
		if (stack1 == null || stack2 == null)
			throw new InternalError();
		if (stack1.length == 0 && stack2.length == 0)
			return 0.0;
		if (stack1.length == 0 || stack2.length == 0)
			return 1.0;
		int mismatches = 0;
		int maxLength = Math.max(stack1.length, stack2.length);
		int minLength = Math.min(stack1.length, stack2.length);
		if (!compareTails) {
			for (int i = 0; i < minLength; i++) {
				if (Math.abs(stack1[i] - stack2[i]) > epsilon)
					mismatches++;
			}
		} else {
			for (int i = 0; i < minLength; i++) {
				if (Math.abs(stack1[stack1.length - i - 1]
						- stack2[stack2.length - i - 1]) > epsilon)
					mismatches++;
			}
		}
		return (maxLength - minLength + mismatches) / (double) maxLength;
	}

}
