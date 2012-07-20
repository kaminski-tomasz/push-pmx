package org.pushpmx;

import java.util.ArrayList;

import org.spiderland.Psh.Interpreter.StackType;
import org.spiderland.Psh.InterpreterState;
import org.spiderland.Psh.booleanStack;
import org.spiderland.Psh.floatStack;
import org.spiderland.Psh.intStack;

/**
 * Semantics for Push individuals behavior.
 * 
 * @author Tomasz Kamiński
 * 
 */
public class Semantics {

	public ArrayList<float[]> stackVector = new ArrayList<float[]>();

	public Semantics() {
	}

	public Semantics(InterpreterState[] states) {
		this(states, StackType.FLOAT_STACK);
	}

	public Semantics(InterpreterState[] states, StackType stackType) {
		for (int i = 0; i < states.length; i++) {
			switch (stackType) {
			case FLOAT_STACK:
				addFloatStack(states[i].getFloatStack());
				break;
			case BOOL_STACK:
				addBooleanStack(states[i].getBoolStack());
				break;
			case INT_STACK:
				addIntStack(states[i].getIntStack());
				break;
			}
		}
	}

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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.hashCode() + " {\n");
		for (int i = 0; i < stackVector.size(); i++) {
			sb.append("\t[");
			for (int j = 0; j < stackVector.get(0).length; j++) {
				if (j > 0) 
					sb.append(",");
				sb.append(stackVector.get(0)[j]);
			}
			sb.append("]\n");
		}
		sb.append("}");
		return sb.toString();
	}
}
