package wci.backend.interpreter.executors;

import java.util.ArrayList;

import wci.backend.interpreter.*;
import wci.intermediate.*;
import wci.intermediate.symtabimpl.Predefined;

import static wci.intermediate.icodeimpl.ICodeKeyImpl.*;
import static wci.intermediate.typeimpl.TypeKeyImpl.ARRAY_ELEMENT_COUNT;


public class AssignmentExecutor extends StatementExecutor
{

	public AssignmentExecutor(Executor parent)
	{
		super(parent);
	}

	public Object execute(ICodeNode node)
	{
		//	The ASSIGN node's children are the target variable
		//	and the expression.
		ArrayList<ICodeNode> children = node.getChildren();
		ICodeNode variableNode = children.get(0);
		ICodeNode expressionNode = children.get(1);
		SymTabEntry variableId = (SymTabEntry) variableNode.getAttribute(ID);

		//	Execute the expression and get its value.
		ExpressionExecutor expressionExecutor = new ExpressionExecutor(this);
		Cell targetCell = (Cell) expressionExecutor.executeVariable(variableNode);
		TypeSpec targetType = variableNode.getTypeSpec();
		TypeSpec valueType = expressionNode.getTypeSpec().baseType();
		Object value = expressionExecutor.execute(expressionNode);

		//	Set the value as an attribute of the variable's symbol table entry.
		assignValue(node, variableId, targetCell, targetType, value, valueType);
		++executionCount;

		return null;
	}

	protected void assignValue(ICodeNode node, SymTabEntry targetId, Cell targetCell, TypeSpec targetType, Object value, TypeSpec valueType) {
		value = checkRange(node, targetType, value);

		if ((targetType == Predefined.realType) && (valueType == Predefined.integerType)) {
			targetCell.setValue(Float.valueOf((Integer) value).intValue());
		} else if (targetType.isPascalString()) {
			int targetLength = (Integer) targetType.getAttribute(ARRAY_ELEMENT_COUNT);
			int valueLength = (Integer) valueType.getAttribute(ARRAY_ELEMENT_COUNT);
			String stringValue = (String) value;

			// If the target is shorter we truncate the value
			// if the target is longer we just pad it with some
			// blank spaces on the right until we get the target length
			if (targetLength < valueLength) {
				stringValue = stringValue.substring(0, targetLength);
			}
			else if (targetLength > valueLength) {
				StringBuilder buffer = new StringBuilder(stringValue);
				for (int i = valueLength; i < targetLength; ++i) {
					buffer.append(" ");
				}
				stringValue = buffer.toString();
			}
			targetCell.setValue(copyOfPascalValue(toPascal(targetType, stringValue), node));
		} else {
			targetCell.setValue(copyOfPascalValue(toPascal(targetType, value), node));
		}
		sendAssignMessage(node, targetId.getName(), value);
	}


}
