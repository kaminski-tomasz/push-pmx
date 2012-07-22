package org.pushpmx.problem;

public class Nguyen5Problem extends FloatSymbolicRegression {

	@Override
	protected double evaluateFunction(double x) {
		return (Math.sin(x * x) * Math.cos(x) - 1.0);
	}

}
