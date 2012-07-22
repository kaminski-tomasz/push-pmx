package org.pushpmx.problem;

public class Keijzer4Problem extends FloatSymbolicRegression {

	@Override
	protected double evaluateFunction(double x) {
		return ((x * x * x) * Math.exp(-x) * Math.cos(x) * Math.sin(x) * (Math
				.sin(x) * Math.sin(x) * Math.cos(x) - 1.0));
	}

}
