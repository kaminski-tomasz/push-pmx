package org.pushpmx.metric;

import org.pushpmx.Semantics;

import ec.EvolutionState;
import ec.Prototype;
import ec.util.Parameter;

public abstract class SemanticsMetric implements Prototype {
	
	public static final String P_EPSILON = "epsilon";
	
	/** Allowed absolute error for compared float values */ 
	public float epsilon = 0.0f;
	
	@Override
	public SemanticsMetric clone() {
		try {
			SemanticsMetric metric = (SemanticsMetric) super.clone();
			return metric;
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}

	@Override
	public void setup(EvolutionState state, Parameter base) {
		Parameter def  = defaultBase();
		epsilon = state.parameters.getFloatWithDefault(base.push(P_EPSILON),
				def.push(P_EPSILON), 0.0f);
	}

	public float getDistance(Semantics sem1, Semantics sem2) {
		int size = sem1.stackVector.size();
		if (size != sem2.stackVector.size())
			return Float.POSITIVE_INFINITY;
		float distance = 0.0f;
		for (int i = 0; i < size; i++) {
			float[] stack1 = sem1.stackVector.get(i);
			float[] stack2 = sem2.stackVector.get(i);
			float stackDistance = getStackDistance(stack1, stack2);
			if (Float.isInfinite(stackDistance))
				return Float.POSITIVE_INFINITY;
			distance += stackDistance;
		}
		if (size > 0)
			distance /= (float) size;
		return distance;
	}

	protected abstract float getStackDistance(float[] stack1, float[] stack2);
}
