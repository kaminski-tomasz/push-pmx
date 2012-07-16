package org.pushpmx.problem;

public class Keijzer4Problem extends FloatSymbolicRegression {

	@Override
	protected float evaluateFunction(float x) {
		return (float) ((x * x * x) * Math.exp(-x) * Math.cos(x) * Math.sin(x) * (Math
				.sin(x) * Math.sin(x) * Math.cos(x) - 1.0f));
	}

}
