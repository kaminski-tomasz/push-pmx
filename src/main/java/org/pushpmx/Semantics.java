/*
* Copyright 2012 Tomasz Kamiński
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

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

	public static final float epsilon = 1.0e-6f;

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
			for (int j = 0; j < stackVector.get(i).length; j++) {
				if (j > 0)
					sb.append(",");
				sb.append(stackVector.get(i)[j]);
			}
			sb.append("]\n");
		}
		sb.append("}");
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Semantics))
			return false;
		Semantics sem = (Semantics) obj;
		if (sem == null || sem.stackVector == null)
			return false;
		if (this.stackVector == sem.stackVector)
			return true;
		if (this.stackVector.size() != sem.stackVector.size())
			return false;
		for (int i = 0; i < stackVector.size(); i++) {
			// check all stacks
			float[] stack1 = this.stackVector.get(i);
			float[] stack2 = sem.stackVector.get(i);
			if (stack1.length != stack2.length)
				return false;
			for (int j = 0; j < stack1.length; j++) {
				// check the relative error
				if (stack1[j] != stack2[j]
						&& Math.abs(stack1[j] - stack2[j]) >= epsilon)
					return false;
			}
		}
		return true;
	}

	public enum CompareType {
		StackHeads, StackTails
	};

	public boolean equalParts(Object obj, CompareType compare) {
		if (obj == this)
			return true;
		if (!(obj instanceof Semantics))
			return false;
		Semantics sem = (Semantics) obj;
		if (sem == null || sem.stackVector == null)
			return false;
		if (this.stackVector == sem.stackVector)
			return true;
		if (this.stackVector.size() != sem.stackVector.size())
			return false;

		for (int i = 0; i < stackVector.size(); i++) {
			// check all stacks
			float[] stack1 = this.stackVector.get(i);
			float[] stack2 = sem.stackVector.get(i);
			int minLength = Math.min(stack1.length, stack2.length);

			if (compare == CompareType.StackHeads) {
				for (int j = 0; j < minLength; j++) {
					if (stack1[j] != stack2[j]
							&& Math.abs(stack1[j] - stack2[j]) >= epsilon)
						return false;
				}
			} else {
				for (int j = 0; j < minLength; j++) {
					if (stack1[stack1.length-j-1] != stack2[stack2.length-j-1]
							&& Math.abs(stack1[stack1.length-j-1] - 
									stack2[stack2.length-j-1]) >= epsilon)
						return false;
				}
			}
		}
		return true;
	}
	
	public boolean equalPeeks(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Semantics))
			return false;
		Semantics sem = (Semantics) obj;
		if (sem == null || sem.stackVector == null)
			return false;
		if (this.stackVector == sem.stackVector)
			return true;
		if (this.stackVector.size() != sem.stackVector.size())
			return false;

		for (int i = 0; i < stackVector.size(); i++) {
			// check all peeks
			float[] stack1 = this.stackVector.get(i);
			float[] stack2 = sem.stackVector.get(i);
			if (stack1.length == 0 && stack2.length == 0)
				continue;
			if (stack1.length == 0 || stack2.length == 0)
				return false;
			float peek1 = stack1[stack1.length - 1];
			float peek2 = stack2[stack2.length - 1];
			if (peek1 != peek2 && Math.abs(peek1 - peek2) >= epsilon)
				return false;
		}
		return true;
	}

}
