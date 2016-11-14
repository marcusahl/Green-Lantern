package wci.backend.interpreter;

import wci.backend.*;
import wci.intermediate.*;
import wci.message.Message;

import static wci.intermediate.icodeimpl.ICodeKeyImpl.*;
import static wci.message.MessageType.*;

/**
 * <h1>RuntimeErrorHandler</h1>
 * 
 * <p>Runtime error handler for the back end interpreter.</p>
 */
public class RuntimeErrorHandler 
{
	private static int MAX_ERROR = 5;
	
	private static int errorCount = 0;				// count of runtime errors
	
	/**
	 * Flag a runtime error.
	 * @param node the root node of the offending statement or expression.
	 * @param errorCode the runtime error code
	 * @param backend the back end processor.
	 */
	public void flag(ICodeNode node, RuntimeErrorCode errorCode, Backend backend)
	{
		String lineNumber = null;
		
		//	Look for the ancestor statement node with a line number attribute
		while ((node != null) && (node.getAttribute(LINE) == null))
		{
			node = node.getParent();
		}
		
		//	Notify the interpreter's listeners.
		backend.sendMessage(new Message(RUNTIME_ERROR, new Object[] 
				{errorCode.toString(), (Integer) node.getAttribute(LINE)}));
		
		if (++errorCount > MAX_ERROR)
		{
			System.out.println("**** ABORTED TOO MANY RUNTIME ERRORS.");
			System.exit(-1);
		}
	}

	public int getErrorCount() 
	{
		return errorCount;
	}
	
	}
