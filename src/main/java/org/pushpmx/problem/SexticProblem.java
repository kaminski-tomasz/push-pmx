package org.pushpmx.problem;

public class SexticProblem extends FloatSymbolicRegression {

	@Override
	protected double evaluateFunction(double x) {
		return x*x*x*x*x*x - 
				2.0 * x*x*x*x + 
				x*x;
	}

}
