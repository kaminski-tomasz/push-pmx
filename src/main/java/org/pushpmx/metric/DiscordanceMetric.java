package org.pushpmx.metric;

public class DiscordanceMetric extends SemanticsMetric {

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
