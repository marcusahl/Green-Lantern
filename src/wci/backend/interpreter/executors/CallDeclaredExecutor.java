package wci.backend.interpreter.executors;

import wci.backend.interpreter.ActivationRecord;
import wci.backend.interpreter.Cell;
import wci.backend.interpreter.Executor;
import wci.backend.interpreter.memorymapimpl.MemoryFactory;
import wci.intermediate.*;

import java.util.ArrayList;

import static wci.intermediate.icodeimpl.ICodeKeyImpl.ID;
import static wci.intermediate.symtabimpl.DefinitionImpl.VALUE_PARM;
import static wci.intermediate.symtabimpl.SymTabKeyImpl.ROUTINE_ICODE;
import static wci.intermediate.symtabimpl.SymTabKeyImpl.ROUTINE_PARMS;

public class CallDeclaredExecutor extends CallExecutor {
    public CallDeclaredExecutor(Executor parent)
    {
        super(parent);
    }

    public Object execute(ICodeNode node) {
        SymTabEntry routineId = (SymTabEntry) node.getAttribute(ID);
        ActivationRecord newRecord = MemoryFactory.createActivationRecord(routineId);

        if (node.getChildren().size() > 0) {
            ICodeNode paramsNode = node.getChildren().get(0);
            ArrayList<ICodeNode> actualNodes = paramsNode.getChildren();
            ArrayList<SymTabEntry> formalIds = (ArrayList<SymTabEntry>) routineId.getAttribute(ROUTINE_PARMS);
            executeActualParams(actualNodes, formalIds, newRecord);
        }
        runtimeStack.push(newRecord);
        sendCallMessage(node, routineId.getName());

        ICode iCode = (ICode) routineId.getAttribute(ROUTINE_ICODE);
        ICodeNode rootNode = iCode.getRoot();

        StatementExecutor statementExecutor = new StatementExecutor(this);
        Object value = statementExecutor.execute(rootNode);

        runtimeStack.pop();
        sendReturnMessage(node, routineId.getName());
        return value;
    }

    private void executeActualParams(
            ArrayList<ICodeNode> actualNodes,
            ArrayList<SymTabEntry> formalIds,
            ActivationRecord record) {

        ExpressionExecutor expressionExecutor = new ExpressionExecutor(this);
        AssignmentExecutor assignmentExecutor = new AssignmentExecutor(this);

        for (int i=0; i < formalIds.size(); ++i) {
            SymTabEntry formalId = formalIds.get(i);
            Definition formalDefinition = formalId.getDefinition();
            Cell formalCell = record.getCell(formalId.getName());
            ICodeNode actualNode = actualNodes.get(i);

            if (formalDefinition == VALUE_PARM) { // Parameters are passed by value
                TypeSpec type = formalId.getTypeSpec();
                TypeSpec valueType = actualNode.getTypeSpec().baseType();
                Object value = expressionExecutor.execute(actualNode);

                assignmentExecutor.assignValue(actualNode, formalId, formalCell, type, value, valueType);
            } else { // Parameters are passed by reference
                Cell actualCell = expressionExecutor.executeVariable(actualNode);
                formalCell.setValue(actualCell);
            }
        }
    }
}
