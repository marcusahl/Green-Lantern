package wci.backend.interpreter;

import wci.backend.Backend;
import wci.intermediate.ICode;
import wci.intermediate.SymTab;
import wci.message.Message;

import static wci.message.MessageType.*;


/**
 * <h1>Executor</h1>
 * 
 *<p>Back end class for the Executor, the central component of the interpreter application.</p>
 */
public class Executor extends Backend {

	public Executor() {
		// TODO Auto-generated constructor stub
	}

	
	/**
	 * Process the intermediate code and the symbol table generated by the 
	 * parser to execute the source program.
	 * @param iCode the intermediate code.
	 * @param symTab the symbol table.
	 * @throws Exception if an error occurred.
	 */
	public void process(ICode iCode, SymTab symTab) throws Exception 
	{
		long startTime = System.currentTimeMillis();
		float elapsedTime = ((System.currentTimeMillis()-startTime)/1000f);
		int executionCount = 0;
		int runtimeErrors = 0;
		
		//Send the compiler summary message
		sendMessage(new Message(INTERPRETER_SUMMARY, new Number[] {executionCount, runtimeErrors, 
				elapsedTime}));
	

	}

}
