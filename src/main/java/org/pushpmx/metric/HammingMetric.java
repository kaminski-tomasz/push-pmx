package org.pushpmx.metric;

public class HammingMetric extends SemanticsMetric {

	@Override
	protected float getStackDistance(float[] stack1, float[] stack2) {
		if (stack1 == null || stack2 == null)
			throw new InternalError();
		if (stack1.length == 0 && stack2.length == 0)
			return 0.0f;
		if (stack1.length != stack2.length)
			return 1.0f;
		for (int i = 0; i < stack1.length; i++) {
			if (stack1[i] != stack2[i])
				return 1.0f;
		}
		return 0.0f;
	}

}
