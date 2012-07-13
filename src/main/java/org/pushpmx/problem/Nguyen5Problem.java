package org.pushpmx.problem;

public class Nguyen5Problem extends FloatSymbolicRegression {

	@Override
	float evaluateFunction(float x) {
		return (float) (Math.sin(x * x) * Math.cos(x) - 1.0f);
	}

}
