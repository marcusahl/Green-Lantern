package main;
import java.io.BufferedReader;
import java.io.FileReader;

import wci.frontend.*;
import wci.intermediate.*;
import wci.backend.*;
import wci.message.*;
import wci.util.*;

import static wci.message.MessageType.*;
import static wci.frontend.pascal.PascalTokenType.*;

/**
 * <h1>Pascal</p>
 * 
 *<p>Compile or interpret a Pascal source program</p>
 */
public class Pascal {
	private Parser parser; // language-independent parser.
	private Source source; // language-independent source.
	private ICode iCode; // generated intermediate code.
	private SymTabStack symTabStack; // generated symbol table.
	private Backend backend; // back end.

	/**
	 * Compile or interpret a Pascal source program.
	 * @param operation
	 * @param filePath
	 * @param flags
	 */
	public Pascal(String operation, String filePath, String flags) 
	{
		try
		{
			boolean intermediate = flags.indexOf('i') > -1; 
			boolean xref		 = flags.indexOf('x') > -1; 
			
			source = new Source(new BufferedReader(new FileReader(filePath)));
			source.addMessageListener(new SourceMessageListener());
			
			parser = FrontendFactory.createParser("Pascal", "top-down", source);
			parser.addMessageListener(new ParserMessageListener());
			
			backend = BackendFactory.createBackend(operation);
			backend.addMessageListener(new BackendMessageListener());
			
			parser.parse();
			source.close();
			
			iCode = parser.getICode();
			symTabStack = parser.getSymTabStack();
			
			if (xref)
			{
				CrossReferencer crossReferencer = new CrossReferencer();
				crossReferencer.print(symTabStack);
			}
			
			if (intermediate)
			{
				ParseTreePrinter treePrinter =
						new ParseTreePrinter(System.out);
				treePrinter.print(iCode);
			}
			
			backend.process(iCode, symTabStack);
			
		}
		catch (Exception ex)
		{
			System.out.println("***** Internal translator error *****");
			ex.printStackTrace();
		}
	}
	
	private static final String FLAGS = "[-ix]";
	private static final String USAGE = "Usage: Pascal execute|comiple " + FLAGS + " <source file path> ";

	/**
	 * The main method.
	 * @param args command-line arguments: "compile" or "execute" followed by
	 * optional flags followed by the source file path.
	 */
	public static void main(String[] args) 
	{
		try
		{
			String operation = args[0];
			
			// Operation.
			if (!(   operation.equalsIgnoreCase("compile")
					|| operation.equalsIgnoreCase("execute")))
			{
				throw new Exception();
			}
			
			int i = 0;
			String flags = "";
			
			//Flags
			while ((++i < args.length) && (args[i].charAt(0) == '-'))
			{
				flags += args[i].substring(1);
			}
			
			// Source path
			if (i < args.length)
			{
				String path = args[i];
				new Pascal(operation, path, flags);
			}
			else
			{
				throw new Exception();
			}
			
		}
		
		catch (Exception ex)
		{
			System.out.println(USAGE);
		}
	}
	
	private static final String SOURCE_LINE_FORMAT = "%03d %s";
	
	private class SourceMessageListener implements MessageListener	
	{
		public SourceMessageListener() {};
		/**
		 * Called by the source whenever it produces a message
		 * @param message the message.
		 */
		public void messageReceived(Message message) 
		{
			MessageType type = message.getType();
			Object body[] = (Object [])message.getBody();
			
			switch (type) 
			{
			
				case SOURCE_LINE: 
				{
					int lineNumber = (Integer) body[0];
					String lineText = (String) body[1];
					
					// We use system.out.println(String.format()) over system.out.printf() here
					// to get each line of the source code printed on a seperate line
					
					System.out.println(String.format(SOURCE_LINE_FORMAT, lineNumber, lineText));
					
					break;
				}

			}
		}
	}
	
	private static final String PARSER_SUMMARY_FORMAT = 
			"\n%,20d source lines." + 
			"\n%,20d syntax errors." + 
			"\n%,20.2f seconds total parsing time.\n";
	private static final String TOKEN_FORMAT =
			">>> %-15s line=%03d, pos=%2d, text=\"%s\"";
	private static final String VALUE_FORMAT = 
			">>> 			value=%s";
	private static final int PREFIX_WIDTH = 5;
	
	/**
	 *	Listener for the parser messages.
	 */
	private class ParserMessageListener implements MessageListener
	{

		/**
		 * Called by the parser whenever it produces a message
		 * @param message the message.
		 */
		public void messageReceived(Message message) 
		{
			MessageType type = message.getType();
			
			switch (type) 
			{
				case TOKEN:
				{
					Object body[] = (Object []) message.getBody();
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
					Object body[] = (Object []) message.getBody();
					int lineNumber = (Integer) body[0];
					int position = (Integer) body[1];
					String tokenText = (String) body[2];
					String errorMessage = (String) body[3];
					
					int spaceCount = PREFIX_WIDTH + position;
					StringBuilder flagBuffer = new StringBuilder();
					
					// Spaces up to the error position
					for (int i = 0; i < spaceCount; i++)
					{
						flagBuffer.append(' ');
					}
					
					// A pointer to the error followed by the error message.
					flagBuffer.append("^\n*** ").append(errorMessage);
					
					//Text, if any, of the bad token
					if (tokenText != null)
					{
						flagBuffer.append(" [at \"").append(tokenText).append("\"]");
					}
					
					System.out.println(flagBuffer.toString());
					break;
				}
			
				case PARSER_SUMMARY: 
				{
					Number body[] = (Number []) message.getBody();
					int statementCount = (Integer) body[0];
					int syntaxErrors = (Integer) body[1];
					float elapsedTime = (Float) body[2];
					
					System.out.printf(PARSER_SUMMARY_FORMAT, statementCount, syntaxErrors, elapsedTime);
					
					break;
				}
			}
		}
	}
	
	private static final String INTERPRETER_SUMMARY_FORMAT = 
			"\n%,20d statements exectued." + 
			"\n%,20d runtime errors" + 
			"\n%,20.2f seconds total execution time.\n";
	
	private static final String COMPILER_SUMMARY_FORMAT =
			"\n%,20d instructions generated." + 
			"\n%,20.2f seconds total code generation time.\n";
	
	private class BackendMessageListener implements MessageListener
	{

		/**
		 * Called by the back end whenever it produces a message
		 * @param message the message
		 */
		public void messageReceived(Message message) 
		{
			MessageType type = message.getType();
			
			switch(type)
			{
				
				case INTERPRETER_SUMMARY:
				{
					Number body[] = (Number[]) message.getBody();
					int executionCount = (Integer) body[0];
					int runtimeErrors = (Integer) body[1];
					float elapsedTime = (Float) body[2];
					
					System.out.printf(INTERPRETER_SUMMARY_FORMAT, executionCount, 
							runtimeErrors, elapsedTime);
					
					break;
				}
				
				case COMPILER_SUMMARY:
				{
					Number body[] = (Number[]) message.getBody();
					int instructionCount = (Integer) body[0];
					float elapsedTime = (Float) body[1];
					
					System.out.printf(COMPILER_SUMMARY_FORMAT, instructionCount, elapsedTime);
					
					break;
				}
			}
		}
		
	}
	
}
