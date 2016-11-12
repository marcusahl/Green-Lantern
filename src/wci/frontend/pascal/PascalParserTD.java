package wci.frontend.pascal;

import wci.frontend.*;
import wci.intermediate.*;
import wci.message.Message;

import static wci.message.MessageType.*;
import static wci.frontend.pascal.PascalTokenType.*;
import static wci.frontend.pascal.PascalErrorCode.*;


/**
 * <h1>PascalParserTD</h1>
 * 
 *<p>The top-down Pascal parser.</p>
 */
public class PascalParserTD extends Parser {
	
	/**
	 * Constructor
	 * @param scanner the scanner to be used for this parser.
	 */
	public PascalParserTD(Scanner scanner) 
	{
		super(scanner);
	}

	protected static PascalErrorHandler errorHandler = new PascalErrorHandler();
	
	/**
	 * Parse a Pascal source program and generate the symbol table and the intermediate code
	 */
	public void parse() 
		throws Exception 
	{
		Token token;
		long startTime = System.currentTimeMillis();
		
		try
		{
			// Loop over each token until end of file
			while (!((token = nextToken()) instanceof EofToken)) 
			{
				TokenType tokenType = token.getType();
				
				// Cross references only the identifiers
				
				if (tokenType == IDENTIFIER)
				{
					String name = token.getText().toLowerCase();
					
					// If not already in the symbol table,
					// create a new entry and enter into the table
					
					SymTabEntry entry = symTabStack.lookup(name);
					if (entry == null)
					{
						entry = symTabStack.enterLocal(name);
					}
					
					// Append the current line number to the entry.
					entry.appendLineNumber(token.getLineNumber());
					
				}
				
				else if (tokenType == ERROR)
				{
					errorHandler.flag(token, (PascalErrorCode) token.getValue(), this);
				}
				
			}
			
			//Send the parser summary message.
			float elapsedTime = (System.currentTimeMillis() - startTime)/1000f;
			sendMessage(new Message(PARSER_SUMMARY, new Number[] {token.getLineNumber(), 
					getErrorCount(), elapsedTime}));
		}
		
		catch (java.io.IOException ex)
		{
			errorHandler.abortTranslation(IO_ERROR, this);
		}

	}

	/**
	 * Return the number of syntax errors found by the parser.
	 * @return the error count
	 */
	public int getErrorCount() 
	{
		return errorHandler.getErrorCount();
	}

}
