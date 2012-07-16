package org.pushpmx.problem;

public class SexticProblem extends FloatSymbolicRegression {

	@Override
	protected float evaluateFunction(float x) {
		return x*x*x*x*x*x - 
				2.0f * x*x*x*x + 
				x*x;
	}

}
