package wci.backend.interpreter.executors;

import java.util.List;

import wci.backend.interpreter.Executor;
import wci.intermediate.ICodeNode;

public class IfExecutor extends StatementExecutor {
	
	private int executionCount;

	public IfExecutor(Executor parent) {
		super(parent);
	}
	
	public Object execute(ICodeNode node) {
		List<ICodeNode> children = node.getChildren();
		ICodeNode exprNode = children.get(0);
		ICodeNode thenStmtNode = children.get(1);
		ICodeNode elseStmtNode = children.size() > 2 ? children.get(2) : null;
		
		ExpressionExecutor expressionExecutor = new ExpressionExecutor(this);
		StatementExecutor statementExecutor = new StatementExecutor(this);
		
		if ((Boolean) expressionExecutor.execute(exprNode)) {
			statementExecutor.execute(thenStmtNode);
		}
		else if (elseStmtNode != null) {
			statementExecutor.execute(elseStmtNode);
		}
		
		++executionCount;
		return null;
	}

}
