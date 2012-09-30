package org.pushpmx.metric;

import org.ecj.psh.PshDefaults;

import ec.EvolutionState;
import ec.util.Parameter;

public class EuclideanMetric extends SemanticsMetric {

	public static final String P_METRIC = "euclidean-metric";
	public static final String P_PARTIAL = "partial";

	public boolean partial;

	@Override
	public Parameter defaultBase() {
		return PshDefaults.base().push(P_METRIC);
	}

	@Override
	public void setup(EvolutionState state, Parameter base) {
		super.setup(state, base);
		Parameter def = defaultBase();
		partial = state.parameters.getBoolean(base.push(P_PARTIAL),
				def.push(P_PARTIAL), false);
	}

	@Override
	protected double getStackDistance(float[] stack1, float[] stack2) {
		if (stack1 == null || stack2 == null)
			throw new InternalError("Stacks objects cannot be null");
		if (stack1.length == 0 && stack2.length == 0) {
			return 0.0;
		}
		if (!partial) {
			if (stack1.length != stack2.length)
				return Double.POSITIVE_INFINITY;
			double distance = 0.0;
			int length = stack1.length;
			for (int i = 0; i < length; i++) {
				double squareDiff = stack1[i] - stack2[i];
				squareDiff = squareDiff * squareDiff;
				distance += squareDiff;
			}
			return Math.sqrt(distance);
		} else {
			if (stack1.length == 0 || stack2.length == 0) {
				return Double.POSITIVE_INFINITY;
			}
			double distance = 0.0;
			int minLength = Math.min(stack1.length, stack2.length);
			for (int i = 0; i < minLength; i++) {
				double squareDiff = stack1[stack1.length - 1 - i]
						- stack2[stack2.length - 1 - i];
				squareDiff = squareDiff * squareDiff;
				distance += squareDiff;
			}
			return Math.sqrt(distance);
		}
	}

}
