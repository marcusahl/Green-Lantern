package wci.backend.interpreter.executors;

import java.util.ArrayList;

import wci.backend.interpreter.Executor;
import wci.intermediate.ICodeNode;

public class CompoundExecutor extends StatementExecutor 
{

	public CompoundExecutor(Executor parent) 
	{
		super(parent);
	}
	
	public Object execute(ICodeNode node)
	{
		//	Loop over the children of the COPOUND node and execute each child.
		StatementExecutor statementExecutor = new StatementExecutor(this);
		ArrayList<ICodeNode> children = node.getChildren();
		
		for (ICodeNode child : children)
		{
			statementExecutor.execute(child);
		}
		
		return null;
	}

}
