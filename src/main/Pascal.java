package main;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import wci.frontend.*;
import wci.intermediate.*;
import wci.backend.*;
import wci.message.*;
import wci.util.*;

import static wci.frontend.pascal.PascalTokenType.*;
import static wci.ide.IDEControl.*;
import static wci.intermediate.symtabimpl.SymTabKeyImpl.*;
import static wci.message.MessageType.SOURCE_LINE;

public class Pascal {
	private Parser parser; 
	private Source source; 
	private ICode iCode; 
	private SymTabStack symTabStack; 
	private Backend backend;

	// FLAGS
	private boolean intermediate; // If intermediate code should be printed
	private boolean xref; // If the cross-reference listing should be printed
	private boolean lines; // If source line tracing should be printed
	private boolean assign; // If value assign tracing should printed
	private boolean fetch; // If value fetch tracing should be printed
	private boolean call; // If routine call tracing should be printed
	private boolean returnn; // If routine return tracing should be printed

	public Pascal(String operation, String filePath, String inputPath, String flags)
	{
		try
		{
			intermediate = flags.indexOf('i') > -1;
			xref = flags.indexOf('x') > -1;
			lines = flags.indexOf('l') > -1;
			assign = flags.indexOf('a') > -1;
			fetch = flags.indexOf('f') > -1;
			call = flags.indexOf('c') > -1;
			returnn = flags.indexOf('r') > -1;

			source = new Source(new BufferedReader(new FileReader(filePath)));
			source.addMessageListener(new SourceMessageListener());
			
			parser = FrontendFactory.createParser("Pascal", "top-down", source);
			parser.addMessageListener(new ParserMessageListener());
			
			backend = BackendFactory.createBackend(operation, inputPath);
			backend.addMessageListener(new BackendMessageListener());
			
			parser.parse();
			source.close();
			
			if (parser.getErrorCount() == 0) {
			
				symTabStack = parser.getSymTabStack();
				SymTabEntry programId = symTabStack.getProgramId();
				iCode = (ICode) programId.getAttribute(ROUTINE_ICODE);
				
				if (xref)
				{
					CrossReferencer crossReferencer = new CrossReferencer();
					crossReferencer.print(symTabStack);
				}
				
				if (intermediate)
				{
					ParseTreePrinter treePrinter = new ParseTreePrinter(System.out);
					treePrinter.print(symTabStack);
				}
			
				backend.process(iCode, symTabStack);
				
			}
							
		}
		
		catch (Exception ex)
		{
			System.out.println("***** Internal translator error *****");
			ex.printStackTrace();
		}
	}
	
	private static final String FLAGS = "[-ixlafcr]";
	private static final String USAGE = "Usage: Pascal execute|comiple " + FLAGS
			+ " <source file path>  <input file path>";

	public static void main(String[] args) 
	{
		try
		{
			String operation = args[0];
			
			// Operation.
			if (!(operation.equalsIgnoreCase("compile") || operation.equalsIgnoreCase("execute")))
			{
				throw new Exception();
			}
			
			int i = 0;
			String flags;
			
			//Flags
			StringBuilder buffer = new StringBuilder();
			while ((++i < args.length) && (args[i].charAt(0) == '-'))
			{
				buffer.append(args[i].substring(1));
			}
			flags = buffer.toString();

			String sourcePath;
			String inputPath = null;

			// Source path
			if (i < args.length)
			{
				sourcePath = args[i];
			} else {
				throw new Exception();
			}
			if (++i < args.length) {
				inputPath = args[i];
				File inputFile = new File(inputPath);
				if (!inputFile.exists()) {
					String errorMessage = "Input file '" + inputPath + "' does not exist.";
					System.out.println(errorMessage);
					throw new Exception(errorMessage);
				}
			}
			new Pascal(operation, sourcePath, inputPath, flags);
		}
		
		catch (Exception ex)
		{
			System.out.println(USAGE);
		}
	}
	
	private static final String SOURCE_LINE_FORMAT = "%03d %s";
	private static final String LINE_FORMAT = ">>> AT LINE %03d\n";
	private static final String PARSER_SUMMARY_FORMAT =
			PARSER_TAG + "%,d source lines, %,d syntax errors, " +
					"%,.2f seconds total parsing time.\n";
	private static final String TOKEN_FORMAT =
			">>> %-15s line=%03d, pos=%2d, text=\"%s\"";
	private static final String VALUE_FORMAT =
			">>> 			value=%s";
	private static final int PREFIX_WIDTH = 5;
	private static final String INTERPRETER_SUMMARY_FORMAT =
			INTERPRETER_TAG + "%,d statements executed, %,d runtime errors, " +
					"%,.2f seconds total execution time.\n";
	private static final String COMPILER_SUMMARY_FORMAT =
			"\n%,20d instructions generated." +
					"\n%,20.2f seconds total code generation time.\n";
	private static final String ASSIGN_FORMAT =
			" >>> LINE %03d: %s = %s\n";
	private static final String FETCH_FORMAT =
			" >>> LINE %03d: %s : %s\n";
	private static final String CALL_FORMAT =
			" >>> LINE %03d: CALL %s\n";
	private static final String RETURN_FORMAT =
			" >>> LINE %03d: RETURN FROM %s\n";

	private class SourceMessageListener implements MessageListener {

		public SourceMessageListener() {}
		public void messageReceived(Message message) 
		{
			MessageType type = message.getType();
			Object[] body = (Object[]) message.getBody();

			if(type == SOURCE_LINE) {
				int lineNumber = (Integer) body[0];
				String lineText = (String) body[1];

				// We use system.out.println(String.format()) over system.out.printf() here
				// to get each line of the source code printed on a seperate line
				System.out.println(String.format(SOURCE_LINE_FORMAT, lineNumber, lineText));
			}
		}
	}
	
	private class ParserMessageListener implements MessageListener {

		public void messageReceived(Message message) 
		{
			MessageType type = message.getType();
			
			switch (type) 
			{
				case TOKEN:
				{
					Object[] body = (Object []) message.getBody();
					int line = (Integer) body[0];
					int position = (Integer) body[1];
					TokenType tokenType = (TokenType) body[2];
					String tokenText = (String) body[3];
					Object tokenValue = (Object) body[4];
					
					// Prints each Token on seperate lines
					System.out.println(String.format(TOKEN_FORMAT, tokenType,
							line, position, tokenText));
					
					if (tokenValue != null)
					{
						if (tokenType == STRING)
						{
							tokenValue = "\"" + tokenValue + "\"";
						}
						
						System.out.println(String.format(VALUE_FORMAT, tokenValue));
					}
					
					break;
				}
				
				case SYNTAX_ERROR:
				{
					Object[] body = (Object []) message.getBody();
					int lineNumber = (Integer) body[0];
					int position = (Integer) body[1];
					String tokenText = (String) body[2];
					String errorMessage = (String) body[3];
					
					int spaceCount = PREFIX_WIDTH + position;
					StringBuilder flagBuffer = new StringBuilder();
					flagBuffer.append(String.format(SYNTAX_TAG + "%d: %s", lineNumber, errorMessage));

					if (tokenText != null)
					{
						flagBuffer.append(" [at \"").append(tokenText).append("\"]");
					}
					
					System.out.println(flagBuffer.toString());
					break;
				}
			
				case PARSER_SUMMARY: 
				{
					Number[] body = (Number []) message.getBody();
					int statementCount = (Integer) body[0];
					int syntaxErrors = (Integer) body[1];
					float elapsedTime = (Float) body[2];
					
					System.out.printf(PARSER_SUMMARY_FORMAT, statementCount, syntaxErrors, elapsedTime);
					
					break;
				}
			}
		}
	}
	
	private class BackendMessageListener implements MessageListener {
		
		public void messageReceived(Message message)
		{
			MessageType type = message.getType();
			
			switch(type)
			{
				case INTERPRETER_SUMMARY: {
					Number[] body = (Number[]) message.getBody();
					int executionCount = (Integer) body[0];
					int runtimeErrors = (Integer) body[1];
					float elapsedTime = (Float) body[2];
					
					System.out.printf(INTERPRETER_SUMMARY_FORMAT, executionCount, 
							runtimeErrors, elapsedTime);
					
					break;
				}
				case COMPILER_SUMMARY: {
					Number[] body = (Number[]) message.getBody();
					int instructionCount = (Integer) body[0];
					float elapsedTime = (Float) body[1];
					
					System.out.printf(COMPILER_SUMMARY_FORMAT, instructionCount, elapsedTime);
					
					break;
				}
				case SOURCE_LINE: {
					if (lines) {
						int lineNumber = (Integer) message.getBody();
						System.out.printf(LINE_FORMAT, lineNumber);
					}
					break;
				}
				case ASSIGN: {
					if (assign) {
						Object[] body = (Object[]) message.getBody();
						int lineNumber = (Integer) body[0];
						String variableName = (String) body[1];
						Object value = String.valueOf((body[2]));
						System.out.printf(ASSIGN_FORMAT, lineNumber, variableName, value);
					}
					break;
				}
				case FETCH: {
					if (fetch) {
						Object[] body = (Object[]) message.getBody();
						int lineNumber = (Integer) body[0];
						String variableName = (String) body[1];
						Object value = String.valueOf((body[2]));
						System.out.printf(FETCH_FORMAT, lineNumber, variableName, value);
					}
					break;
				}
				case CALL: {
					if (call) {
						Object[] body = (Object[]) message.getBody();
						int lineNumber = (Integer) body[0];
						String routineName = String.valueOf((body[1]));
						System.out.printf(CALL_FORMAT, lineNumber, routineName);
					}
					break;
				}
				case RETURN: {
					if (returnn) {
						Object[] body = (Object[]) message.getBody();
						int lineNumber = (Integer) body[0];
						String routineName = String.valueOf((body[1]));
						System.out.printf(RETURN_FORMAT, lineNumber, routineName);
					}
					break;
				}
				case RUNTIME_ERROR:
				{
					Object[] body = (Object []) message.getBody();
					String errorMessage = (String) body[0];
					Integer lineNumber = (Integer) body[1];
					
					System.out.print("**** RUNTIME ERROR");
					if (lineNumber != null)
					{
						System.out.print(" AT LINE " + String.format("%03d", lineNumber));
					}
					System.out.println(": " + errorMessage);
					break;
				}
			}
		}
	}
}
