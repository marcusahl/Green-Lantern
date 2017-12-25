package wci.backend.interpreter.executors;

import static wci.intermediate.icodeimpl.ICodeKeyImpl.VALUE;

import java.util.ArrayList;
import java.util.HashMap;

import wci.backend.interpreter.Executor;
import wci.intermediate.ICodeNode;

public class SelectExecutor extends StatementExecutor {

	private int executionCount;
	private static HashMap<ICodeNode, HashMap<Object, ICodeNode>> jumpCache =
			new HashMap<ICodeNode, HashMap<Object, ICodeNode>>();
	
	public SelectExecutor(Executor parent) {
		super(parent);
	}
	
	public Object execute(ICodeNode node) {
		HashMap<Object, ICodeNode> jumpTable = jumpCache.get(node);
		if (jumpTable == null) {
			jumpTable = createJumpTable(node);
			jumpCache.put(node, jumpTable);
		}
		
		ArrayList<ICodeNode> selectChildren = node.getChildren();
		ICodeNode exprNode = selectChildren.get(0);
		
		ExpressionExecutor expressionExecutor = new ExpressionExecutor(this);
		Object selectValue = expressionExecutor.execute(exprNode);
		ICodeNode statementNode = jumpTable.get(selectValue);
		
		if (statementNode != null) {
			StatementExecutor statementExecutor = new StatementExecutor(this);
			statementExecutor.execute(statementNode);
		}		
		
		++executionCount;
		return null;
	}
	
	private HashMap<Object, ICodeNode> createJumpTable(ICodeNode node) {
		
		HashMap<Object, ICodeNode> jumpTable = new HashMap<Object, ICodeNode>();
		
		ArrayList<ICodeNode> selectChildren = node.getChildren();
		for (int i = 1; i < selectChildren.size(); ++i) {
			ICodeNode branchNode = selectChildren.get(i);
			populateJumpTable(jumpTable, branchNode);
		}
		
		return jumpTable;
	}
	
	private void populateJumpTable(HashMap<Object, ICodeNode> jumpTable, ICodeNode branchNode) {
		// The branch node has two children. The constant sub-tree, and the statement sub-tree.
		ArrayList<ICodeNode> constantList = branchNode.getChildren().get(0).getChildren();
		ICodeNode statementNode = branchNode.getChildren().get(1);
		
		for (ICodeNode constantNode : constantList) {	
			Object value = constantNode.getAttribute(VALUE);
			jumpTable.put(value, statementNode);
		}
	}

}
