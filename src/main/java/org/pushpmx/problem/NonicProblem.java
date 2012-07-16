package org.pushpmx.problem;

public class NonicProblem extends FloatSymbolicRegression {

	@Override
	protected float evaluateFunction(float x) {
		return x * x * x * x * x * x * x * x * x+
				x * x * x * x * x * x * x * x +
				x * x * x * x * x * x * x +
				x * x * x * x * x * x +
				x * x * x * x * x +
				x * x * x * x + 
				x * x * x + 
				x * x + 
				x;
	}

}
