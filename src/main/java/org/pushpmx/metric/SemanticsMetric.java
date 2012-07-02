package org.pushpmx.metric;

import org.pushpmx.Semantics;

public abstract class SemanticsMetric {

	public float getDistance(Semantics sem1, Semantics sem2) {
		int size = sem1.stackVector.size();
		if (size != sem2.stackVector.size())
			return Float.POSITIVE_INFINITY;
		float distance = 0.0f;
		for (int i = 0; i < size; i++) {
			float[] stack1 = sem1.stackVector.get(i);
			float[] stack2 = sem2.stackVector.get(i);
			float stackDistance = getStackDistance(stack1, stack2);
			if (stackDistance == Float.POSITIVE_INFINITY)
				return Float.POSITIVE_INFINITY;
			distance += stackDistance;
		}
		if (size > 0)
			distance /= (float) size;
		return distance;
	}

	protected abstract float getStackDistance(float[] stack1, float[] stack2);
}
