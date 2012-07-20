package org.pushpmx.metric;

import org.ecj.psh.PshDefaults;

import ec.util.Parameter;

public class DiscordanceMetric extends SemanticsMetric {

	public static final String P_METRIC = "discordance-metric";

	@Override
	public Parameter defaultBase() {
		return PshDefaults.base().push(P_METRIC);
	}

	@Override
	protected float getStackDistance(float[] stack1, float[] stack2) {
		if (stack1.length == 0 && stack2.length == 0)
			return 0.0f;
		if (stack1.length == 0 || stack2.length == 0)
			return 1.0f;
		int mismatches = 0;
		int maxLength = Math.max(stack1.length, stack2.length);
		int minLength = Math.min(stack1.length, stack2.length);
		for (int i = 0; i < minLength; i++) {
			if (stack1[i] != stack2[i])
				mismatches++;
		}
		return (maxLength - minLength + mismatches) / (float) maxLength;
	}

}
