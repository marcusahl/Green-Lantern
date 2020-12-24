package wci.backend.interpreter.executors;

import java.util.List;

import wci.backend.interpreter.Executor;
import wci.intermediate.ICodeNode;
import wci.intermediate.icodeimpl.ICodeNodeTypeImpl;

import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.TEST;

public class LoopExecutor extends StatementExecutor {

	private int executeLoopCount;
	
	public LoopExecutor(Executor parent) {
		super(parent);
	}
	
	public Object execute(ICodeNode node) {
		
		boolean exitLoop = false;
		ICodeNode exprNode = null;
		List<ICodeNode> loopChildren = node.getChildren();

		ExpressionExecutor expressionExecutor = new ExpressionExecutor(this);
		StatementExecutor statementExecutor = new StatementExecutor(this);
		
		while (!exitLoop) {
			++executeLoopCount;
			
			for (ICodeNode child : loopChildren) {
				ICodeNodeTypeImpl childType = (ICodeNodeTypeImpl) child.getType();
				
				if (childType == TEST) {
					if (exprNode == null) {
						exprNode = child.getChildren().get(0);
					}
					exitLoop = (Boolean) expressionExecutor.execute(exprNode);
				}
				else {
					statementExecutor.execute(child);
				}
				
				if(exitLoop) {
					break;
				}
			}
		}
		
		return null;
	}

}
