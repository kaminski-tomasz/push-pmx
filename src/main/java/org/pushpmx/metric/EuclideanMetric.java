package org.pushpmx.metric;

public class EuclideanMetric extends SemanticsMetric {

	@Override
	protected float getStackDistance(float[] stack1, float[] stack2) {
		if (stack1 == null && stack2 == null)
			return 0.0f;
		if (stack1 == null || stack2 == null || stack1.length != stack2.length)
			return Float.POSITIVE_INFINITY;
		double distance = 0.0;
		int length = stack1.length;
		for (int i = 0; i < length; i++) {
			double squareDiff = stack1[i] - stack2[i];
			squareDiff = squareDiff * squareDiff;
			distance += squareDiff;
		}
		return (float) Math.sqrt(distance);
	}

}
