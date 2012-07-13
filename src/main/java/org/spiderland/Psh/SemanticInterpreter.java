package org.spiderland.Psh;

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
	 * Stack history traced after each done step
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
	}

	@Override
	public void ClearStacks() {
		super.ClearStacks();
		_trace.clear();
	}
}
