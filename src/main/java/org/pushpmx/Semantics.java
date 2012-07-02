package org.pushpmx;

import java.util.ArrayList;
import java.util.Arrays;

import org.spiderland.Psh.*;

/**
 * Semantics for Push individuals behaviour. For each test case holds float
 * array according to appropriate stack obtained from interpreter.
 * 
 * @author Tomasz Kamiński
 * 
 */
public class Semantics {

	public ArrayList<float[]> stackVector = new ArrayList<float[]>();

	/**
	 * Maps float stack to float array according to current test case
	 * 
	 * @param stack
	 */
	public void addFloatStack(floatStack stack) {
		float[] newStack = null;
		int size = stack.size();
		newStack = new float[size];
		for (int i = 0; i < size; i++) {
			newStack[i] = stack.peek(i);
		}
		stackVector.add(newStack);
	}

	/**
	 * Maps int stack to float array according to current test case
	 * 
	 * @param stack
	 */
	public void addIntStack(intStack stack) {
		float[] newStack = null;
		int size = stack.size();
		newStack = new float[size];
		for (int i = 0; i < size; i++) {
			newStack[i] = (float) stack.peek(i);
		}
		stackVector.add(newStack);
	}

	/**
	 * Maps boolean stack to float array according to current test case. true is
	 * mapped into 1 and false into 0 accordingly.
	 * 
	 * @param stack
	 */
	public void addBooleanStack(booleanStack stack) {
		float[] newStack = null;
		int size = stack.size();
		newStack = new float[size];
		for (int i = 0; i < size; i++) {
			newStack[i] = stack.peek(i) ? 1.0f : 0.0f;
		}
		stackVector.add(newStack);
	}

	@Override
	public Semantics clone() {
		Semantics sem = new Semantics();
		sem.stackVector = new ArrayList<float[]>(this.stackVector.size());
		for (float[] stack : this.stackVector) {
			if (stack == null) {
				sem.stackVector.add(null);
			} else {
				sem.stackVector.add(stack.clone());
			}
		}
		return sem;
	}

}
