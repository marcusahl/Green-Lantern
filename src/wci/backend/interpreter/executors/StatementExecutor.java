package wci.backend.interpreter.executors;

import wci.backend.interpreter.*;
import wci.intermediate.*;
import wci.intermediate.icodeimpl.*;
import wci.message.*;

import static wci.message.MessageType.*;
import static wci.backend.interpreter.RuntimeErrorCode.*;
import static wci.intermediate.icodeimpl.ICodeKeyImpl.*;

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
		case IF: {}
		case SELECT: {}
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

}