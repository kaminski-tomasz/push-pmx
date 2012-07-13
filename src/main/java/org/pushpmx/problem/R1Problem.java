package org.pushpmx.problem;

public class R1Problem extends FloatSymbolicRegression {

	@Override
	float evaluateFunction(float x) {
		return (x + 1) * (x + 1) * (x + 1) / (x * x - x + 1);
	}

}
