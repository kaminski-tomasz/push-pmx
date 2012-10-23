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

package org.spiderland.Psh;

public class InterpreterState implements Cloneable {
	private intStack intStack = null;
	private floatStack floatStack = null;
	private booleanStack boolStack = null;
	private ObjectStack codeStack = null;
	private ObjectStack nameStack = null;
	private ObjectStack inputStack = null;
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		InterpreterState state = (InterpreterState) obj;
		boolean result = true;
		result &= state.floatStack.equals(floatStack);
		result &= state.boolStack.equals(boolStack);
		result &= state.intStack.equals(intStack);
		result &= state.inputStack.equals(inputStack);
		result &= state.nameStack.equals(nameStack);
		result &= state.codeStack.equals(codeStack);
		return result;
	}

	public InterpreterState() {
		this.intStack = new intStack();
		this.floatStack = new floatStack();
		this.boolStack = new booleanStack();
		this.codeStack = new ObjectStack();
		this.nameStack = new ObjectStack();
		this.inputStack = new ObjectStack();
	}

	public InterpreterState(Interpreter interp) {
		this.intStack = interp._intStack;
		this.floatStack = interp._floatStack;
		this.boolStack = interp._boolStack;
		this.nameStack = interp._nameStack;
		this.codeStack = interp._codeStack;
		this.inputStack = interp._inputStack;
	}

	@Override
	public InterpreterState clone() {
		InterpreterState state = null;
		try {
			state = (InterpreterState) super.clone();
			if (intStack != null)
				state.intStack = intStack.clone();
			if (floatStack != null)
				state.floatStack = floatStack.clone();
			if (boolStack != null)
				state.boolStack = boolStack.clone();
			if (codeStack != null)
				state.codeStack = codeStack.clone();
			if (nameStack != null)
				state.nameStack = nameStack.clone();
			if (inputStack != null)
				state.setInputStack(getInputStack().clone());
			return state;
		} catch (CloneNotSupportedException e) {
			throw new InternalError(); // never happens
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("\n");
		sb.append("{F: ").append(floatStack).append(", IN: ")
				.append(inputStack).append("}").append(", B: ")
				.append(boolStack).append(", INT: ").append(intStack);
		return sb.toString();
	}
	
	public intStack getIntStack() {
		return intStack;
	}

	public void setIntStack(intStack intStack) {
		this.intStack = intStack;
	}

	public floatStack getFloatStack() {
		return floatStack;
	}

	public void setFloatStack(floatStack floatStack) {
		this.floatStack = floatStack;
	}

	public booleanStack getBoolStack() {
		return boolStack;
	}

	public void setBoolStack(booleanStack boolStack) {
		this.boolStack = boolStack;
	}

	public ObjectStack getCodeStack() {
		return codeStack;
	}

	public void setCodeStack(ObjectStack codeStack) {
		this.codeStack = codeStack;
	}

	public ObjectStack getNameStack() {
		return nameStack;
	}

	public void setNameStack(ObjectStack nameStack) {
		this.nameStack = nameStack;
	}

	public ObjectStack getInputStack() {
		return inputStack;
	}

	public void setInputStack(ObjectStack _inputStack) {
		this.inputStack = _inputStack;
	}
}
