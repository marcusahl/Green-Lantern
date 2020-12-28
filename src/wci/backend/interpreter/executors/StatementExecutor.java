package wci.backend.interpreter.executors;

import wci.backend.interpreter.*;
import wci.backend.interpreter.memorymapimpl.MemoryFactory;
import wci.intermediate.*;
import wci.intermediate.icodeimpl.*;
import wci.intermediate.symtabimpl.Predefined;
import wci.message.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static wci.backend.interpreter.RuntimeErrorCode.*;
import static wci.intermediate.icodeimpl.ICodeKeyImpl.*;
import static wci.intermediate.typeimpl.TypeFormImpl.SUBRANGE;
import static wci.intermediate.typeimpl.TypeKeyImpl.SUBRANGE_MAX_VALUE;
import static wci.intermediate.typeimpl.TypeKeyImpl.SUBRANGE_MIN_VALUE;
import static wci.message.MessageType.*;

public class StatementExecutor extends Executor 
{
	
	public StatementExecutor(Executor parent) 
	{
		super(parent);
	}
	
	public Object execute(ICodeNode node)
	{
		ICodeNodeTypeImpl nodeType = (ICodeNodeTypeImpl) node.getType();
		
		//	Send a message about the current source line.
		sendSourceLineMessage(node);
		
		// TODO this switch has a lot of code repetition that should be re-factored
		switch (nodeType)
		{
		
		case COMPOUND:{
			CompoundExecutor compoundExecutor = new CompoundExecutor(this);
			return compoundExecutor.execute(node);
		}
			
		case ASSIGN: {
			AssignmentExecutor assignmentExecutor = new AssignmentExecutor(this);
			return assignmentExecutor.execute(node);
		}
			
		case LOOP:{
			LoopExecutor loopExecutor = new LoopExecutor(this);
			return loopExecutor.execute(node);
		}
		case IF: {
			IfExecutor ifExecutor = new IfExecutor(this);
			return ifExecutor.execute(node);
		}
		case SELECT: {
			SelectExecutor selectExecutor = new SelectExecutor(this);
			return selectExecutor.execute(node);
		}
		case CALL: {
			CallExecutor callExecutor = new CallExecutor(this);
			return callExecutor.execute(node);
		}

		case NO_OP: return null;
					
		default:
			{
				errorHandler.flag(node, UNIMPLEMENTED_FEATURE, this);
				return null;
			}
		}
	}

	private void sendSourceLineMessage(ICodeNode node)
	{
		Object lineNumber = node.getAttribute(LINE);
		
		//	Send the SOURCE_LINE message.
		if (lineNumber != null)
		{
			sendMessage(new Message(SOURCE_LINE, lineNumber));
		}
	}

	protected Object toPascal(TypeSpec targetType, Object javaValue) {

		if (javaValue instanceof String) {
			String string = (String) javaValue;

			if (targetType == Predefined.charType) {
				return string.charAt(0);
			}
			else if (targetType.isPascalString()) {
				Cell charCells[] = new Cell[string.length()];
				for(int i = 0; i < string.length(); i++) charCells[i] = MemoryFactory.createCell(string.charAt(i));
				return charCells;
			}
		}

		return javaValue;
	}

	protected Object toJava(TypeSpec targetType, Object pascalValue) {

		if (pascalValue instanceof Cell[] && ((Cell[]) pascalValue)[0].getValue() instanceof Character) {
			Cell charCells[] = (Cell[]) pascalValue;
			StringBuilder stringBuilder = new StringBuilder(charCells.length);

			for (Cell cell : charCells) stringBuilder.append(cell.getValue());
			return stringBuilder.toString();
		}
		return pascalValue;
	}

	protected Object copyOfPascalValue(Object value, ICodeNode node) {
		Object copy;

		if (value instanceof Integer) {
			copy = Integer.valueOf((Integer) value);
		} else if (value instanceof Float) {
			copy = Float.valueOf((Float) value);
		} else if (value instanceof Character) {
			copy = Character.valueOf((Character) value);
		} else if (value instanceof Boolean) {
			copy = Boolean.valueOf((Boolean) value);
		} else if (value instanceof String) {
			copy = String.valueOf((String) value);
		} else if (value instanceof HashMap) {
			copy = copyPascalRecord((HashMap<String, Object>) value, node);
		} else {
			copy = copyPascalArray((Cell[]) value, node);
		}

		return copy;
	}

	private Object copyPascalRecord(HashMap<String, Object> value, ICodeNode node) {
		HashMap<String, Object> copy = new HashMap<String, Object>();

		if (value != null) {
			Set<Map.Entry<String, Object>> entries = value.entrySet();
			Iterator<Map.Entry<String, Object>> it = entries.iterator();

			while(it.hasNext()) {
				Map.Entry<String, Object> entry = it.next();
				String newKey = new String(entry.getKey());
				Cell valueCell = (Cell) entry.getValue();
				Object newValue = copyOfPascalValue(valueCell.getValue(), node);
				copy.put(newKey, MemoryFactory.createCell(newValue));
			}
		} else {
			errorHandler.flag(node, UNINITIALIZED_VALUE, this);
		}
		return copy;
	}

	private Object copyPascalArray(Cell[] valueCells, ICodeNode node) {
		int length;
		Cell[] copy;
		if(valueCells != null) {
			length = valueCells.length;
			copy = new Cell[length];
			for (int i = 0; i < length; i++) {
				Cell valueCell = valueCells[i];
				Object newValue = copyOfPascalValue(valueCell.getValue(), node);
				copy[i] = MemoryFactory.createCell(newValue);
			}

		} else {
			errorHandler.flag(node, UNINITIALIZED_VALUE, this);
			copy = new Cell[1];
		}

		return copy;
	}

	protected Object checkRange(ICodeNode node, TypeSpec type, Object value) {
		if (type.getForm() == SUBRANGE) {
			int minValue = (Integer) type.getAttribute(SUBRANGE_MIN_VALUE);
			int maxValue = (Integer) type.getAttribute(SUBRANGE_MAX_VALUE);

			if ((Integer) value < minValue) {
				errorHandler.flag(node, VALUE_RANGE, this);
				return minValue;
			}
			else if ((Integer) value > maxValue) {
				errorHandler.flag(node, VALUE_RANGE, this);
				return maxValue;
			}
		}
		return value;
	}

	protected void sendAssignMessage(ICodeNode node, String variableName, Object value) {
		Object lineNumber = getLineNumber(node);
		if (lineNumber != null) {
			sendMessage(new Message(ASSIGN, new Object[] {lineNumber, variableName, value}));
		}
	}

	protected void sendFetchMessage(ICodeNode node, String variableName, Object value) {
		Object lineNumber = getLineNumber(node);
		if (lineNumber != null) {
			sendMessage(new Message(FETCH, new Object[] {lineNumber, variableName, value}));
		}
	}

	protected void sendCallMessage(ICodeNode node, String routineName) {
		Object lineNumber = getLineNumber(node);
		if (lineNumber != null) {
			sendMessage(new Message(CALL, new Object[] {lineNumber, routineName}));
		}
	}

	protected void sendReturnMessage(ICodeNode node, String routineName) {
		Object lineNumber = getLineNumber(node);
		if (lineNumber != null) {
			sendMessage(new Message(RETURN, new Object[] {lineNumber, routineName}));
		}
	}

	private Object getLineNumber(ICodeNode node) {
		while (node != null && node.getAttribute(LINE) == null) {
			node = node.getParent();
		}
		return node != null ? node.getAttribute(LINE) : null;
	}


}
