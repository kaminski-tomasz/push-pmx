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
	protected float getStackDistance(float[] stack1, float[] stack2) {
		if (stack1 == null || stack2 == null)
			throw new InternalError();
		if (stack1.length == 0 && stack2.length == 0)
			return 0.0f;
		if (stack1.length == 0 || stack2.length == 0)
			return 1.0f;
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
		return (maxLength - minLength + mismatches) / (float) maxLength;
	}

}
