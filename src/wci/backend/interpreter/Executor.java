package wci.backend.interpreter;

import main.Pascal;
import wci.backend.Backend;
import wci.backend.BackendFactory;
import wci.backend.interpreter.executors.*;
import wci.backend.interpreter.memorymapimpl.MemoryFactory;
import wci.frontend.Scanner;
import wci.frontend.Source;
import wci.frontend.pascal.PascalScanner;
import wci.frontend.pascal.parsers.CallDeclaredParser;
import wci.intermediate.*;
import wci.intermediate.icodeimpl.ICodeNodeTypeImpl;
import wci.message.Message;

import java.io.*;

import static wci.backend.interpreter.debuggerimpl.DebuggerType.COMMAND_LINE;
import static wci.intermediate.icodeimpl.ICodeKeyImpl.ID;
import static wci.message.MessageType.*;

public class Executor extends Backend {

	protected static int executionCount;
	protected static RuntimeStack runtimeStack;
	protected static RuntimeErrorHandler errorHandler;
	protected static Scanner standardIn;
	protected static PrintWriter standardOut;

	protected Debugger debugger;

	static
	{
		executionCount = 0;
		runtimeStack = MemoryFactory.createRuntimeStack();
		errorHandler = new RuntimeErrorHandler();
		standardOut = new PrintWriter(new PrintStream(System.out));

	}

	public void process(ICode iCode, SymTabStack symTabStack) throws Exception 
	{
		this.symTabStack = symTabStack;
		long startTime = System.currentTimeMillis();

		SymTabEntry programId = symTabStack.getProgramId();
		
		ICodeNode callNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.CALL);
		callNode.setAttribute(ID, programId);

		CallDeclaredExecutor callExecutor = new CallDeclaredExecutor(this);
		callExecutor.execute(callNode);
		
		float elapsedTime = ((System.currentTimeMillis()-startTime)/1000f);
		int runtimeErrors = errorHandler.getErrorCount();
		
		sendMessage(new Message(INTERPRETER_SUMMARY, new Number[] {executionCount, runtimeErrors, elapsedTime}));

	}

	public Executor(String inputPath) {
		try {
			standardIn = inputPath != null
					? new PascalScanner(new Source(new BufferedReader(new FileReader(inputPath))))
					: new PascalScanner(new Source(new BufferedReader(new InputStreamReader(System.in))));
		} catch (IOException swallowed) {}
		debugger = BackendFactory.createDebugger(COMMAND_LINE, this, runtimeStack);
	}

	public Executor(Executor parent)
	{
		super();
		this.debugger = parent.debugger;
	}

}
