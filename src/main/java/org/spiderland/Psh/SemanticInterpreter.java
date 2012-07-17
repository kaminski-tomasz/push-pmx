package org.spiderland.Psh;

import java.util.ArrayList;

import ec.EvolutionState;
import ec.util.Parameter;

/**
 * Semantic interpreter. Executes instructions and traces stack after each
 * instruction at the "top" level of program.
 * 
 * @author tomek
 * 
 */
public class SemanticInterpreter extends Interpreter {

	public static final String P_TRACEDSTACK = "traced-stack";
	
	public ArrayList<String> instructionNames = new ArrayList<String>(30);
	
	/**
	 * The stack to be traced. By default, none of the stacks is traced
	 */
	protected StackType _stackToBeTraced = null;

	public ObjectStack getTrace() {
		return _trace;
	}
	public void setTrace(ObjectStack _trace) {
		this._trace = _trace;
	}

	/**
	 * Stack history traced after each step
	 */
	public ObjectStack _trace = new ObjectStack();

	@Override
	public void setup(EvolutionState state, Parameter base) {
		super.setup(state, base);
		
		Parameter def = defaultBase();

		String tracedStack = state.parameters.getStringWithDefault(
				base.push(P_TRACEDSTACK), def.push(P_TRACEDSTACK), "null");

		if (tracedStack.equals("float"))
			_stackToBeTraced = StackType.FLOAT_STACK;
		else if (tracedStack.equals("int"))
			_stackToBeTraced = StackType.INT_STACK;
		else if (tracedStack.equals("bool"))
			_stackToBeTraced = StackType.BOOL_STACK;
		else
			_stackToBeTraced = null;

	}
		
	@Override
	public void SetInstructions(Program inInstructionList)
			throws RuntimeException {
		super.SetInstructions(inInstructionList);
		for (Object instr : _generators.keySet().toArray()) {
			String in = (String) instr;
			if (_randomGenerators.contains(_generators
					.get(in)) && !in.contains(".erc"))
				instructionNames.add(in);
		}
	}
	
	/**
	 * Executes a Push program with a given instruction limit.
	 * 
	 * @param inMaxSteps
	 *            The maximum number of instructions allowed to be executed.
	 * @return The number of instructions executed.
	 * @throws Exception
	 */
	@Override
	public int Execute(Program inProgram, int inMaxSteps) {
		return super.Execute(inProgram, inMaxSteps);
		/*
		_evaluationExecutions++;
		_codeStack.push(inProgram);

		int availableSteps = inMaxSteps;
		int totalSteps = 0;

		for (int i = 0; i < inProgram.size(); i++) {
			_execStack.push(inProgram.peek(i));
			int steps = Step(availableSteps);
			availableSteps -= steps;
			totalSteps += steps;

			if (_stackToBeTraced != null)
				switch (_stackToBeTraced) {
				case INT_STACK:
					_trace.push(_intStack.clone());
					break;
				case FLOAT_STACK:
					_trace.push(_floatStack.clone());
					break;
				case BOOL_STACK:
					_trace.push(_boolStack.clone());
					break;
				default:
					break;
				}
		}

		return totalSteps;
		*/
	}

	@Override
	public void ClearStacks() {
		super.ClearStacks();
		_trace.clear();
	}
	
//	public void RestoreStacks() {
//		_floatFrameStack.pop();
//		_intFrameStack.pop();
//		_boolFrameStack.pop();
//		_codeFrameStack.pop();
//		_nameFrameStack.pop();
//		
//		_floatFrameStack.push(((floatStack)_floatFrameStack.top()).clone());
//		_intFrameStack.push(((intStack)_intFrameStack.top()).clone());
//		_boolFrameStack.push(((booleanStack)_boolFrameStack.top()).clone());
//		_codeFrameStack.push(((ObjectStack)_codeFrameStack.top()).clone());
//		_nameFrameStack.push(((ObjectStack)_nameFrameStack.top()).clone());
//		
//		AssignStacksFromFrame();
//	}
	
	public class InterpreterState {
		protected intStack _intStack = null;
		protected floatStack _floatStack = null;
		protected booleanStack _boolStack = null;
		
		/** Saves the interpreter's value stacks */
		public void save() {
			this._intStack = SemanticInterpreter.this._intStack.clone();
			this._floatStack = SemanticInterpreter.this._floatStack.clone();
			this._boolStack = SemanticInterpreter.this._boolStack.clone();
		}
		
		/**
		 * Restores value stacks. WARNING: this is not compatible with
		 * push frame mode (because the new stacks are not in frame stacks) - I
		 * don't need this functionality for now
		 */
		public void restore() {
			SemanticInterpreter.this._intStack = _intStack.clone();
			SemanticInterpreter.this._floatStack = _floatStack.clone();
			SemanticInterpreter.this._boolStack = _boolStack.clone();
			
			UpdateStackInstructions(StackType.FLOAT_STACK);
			UpdateStackInstructions(StackType.INT_STACK);
			UpdateStackInstructions(StackType.BOOL_STACK);
		}
	}
}
