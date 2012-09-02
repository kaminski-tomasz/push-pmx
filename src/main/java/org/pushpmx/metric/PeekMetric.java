package org.pushpmx.metric;

import org.ecj.psh.PshDefaults;

import ec.EvolutionState;
import ec.util.Parameter;

public class PeekMetric extends SemanticsMetric {

	public static final String P_METRIC = "peek-metric";
	public static final String P_TYPE = "type";

	public static final int V_HAMMING = 0;
	public static final int V_EUCLIDEAN = 1;

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
		type = V_HAMMING;
		if ("hamming".equals(typeStr)) {
			type = V_HAMMING;
		} else if ("euclidean".equals(typeStr)) {
			type = V_EUCLIDEAN;
		} else if (typeStr != null) {
			state.output.fatal("Unknown peek metric type: " + typeStr);
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
			case V_HAMMING:
				return 1.0;
			case V_EUCLIDEAN:
				return Double.POSITIVE_INFINITY;
			}
		}
		float peek1 = stack1[stack1.length - 1];
		float peek2 = stack2[stack2.length - 1];
		switch (type) {
		case V_EUCLIDEAN:
			return Math.abs(peek1 - peek2);
		default:
		case V_HAMMING:
			if (Math.abs(peek1 - peek2) > epsilon)
				return 1.0;
			return 0.0;
		}
	}

}
