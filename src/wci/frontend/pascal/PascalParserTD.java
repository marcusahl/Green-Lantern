package wci.frontend.pascal;

import static wci.frontend.pascal.PascalErrorCode.IO_ERROR;
import static wci.frontend.pascal.PascalErrorCode.UNEXPECTED_TOKEN;
import static wci.message.MessageType.PARSER_SUMMARY;

import java.util.EnumSet;

import org.jetbrains.annotations.NotNull;
import wci.frontend.EofToken;
import wci.frontend.Parser;
import wci.frontend.Scanner;
import wci.frontend.Token;
import wci.frontend.pascal.parsers.ProgramParser;
import wci.intermediate.ICode;
import wci.intermediate.ICodeFactory;
import wci.intermediate.SymTabEntry;
import wci.intermediate.symtabimpl.Predefined;
import wci.message.Message;

public class PascalParserTD extends Parser {
	
	private SymTabEntry routineId;
	
	protected static PascalErrorHandler errorHandler = new PascalErrorHandler();
	
	public PascalParserTD(Scanner scanner) 
	{
		super(scanner);
	}
	
	public PascalParserTD(PascalParserTD parent)
	{
		super(parent.getScanner());
	}
	
	public void parse() 
		throws Exception 
	{
		long startTime = System.currentTimeMillis();
		Predefined.initialize(symTabStack);
		
		try
		{
			// Parse the program
			Token token = nextToken();
			ProgramParser programParser = new ProgramParser(this);
			programParser.parse(token, null);
			token = currentToken();

			// Sends the parser's summary message on the output
			float elapsedTime = (System.currentTimeMillis() - startTime)/1000f;
			sendMessage(new Message(PARSER_SUMMARY, new Number[] {token.getLineNumber(), 
					getErrorCount(), elapsedTime}));
		}
		
		catch (java.io.IOException ex)
		{
			errorHandler.abortTranslation(IO_ERROR, this);
		}

	}

	public Token synchronize(@NotNull EnumSet syncSet)
		throws Exception {
		
		Token token = currentToken();
		
		// If the current token is not in the synchronization
		// set, then it is unexpected and the parser must recover
		if (!syncSet.contains(token.getType())) {
			errorHandler.flag(token, UNEXPECTED_TOKEN, this);
			
			// We recover by skipping until we find a token
			// in the synchronization set
			do {
				token = nextToken();
			} while (!(token instanceof EofToken) &&
					!syncSet.contains(token.getType()));
		}
		
		return token;
		
	}
	
	public int getErrorCount() 
	{
		return errorHandler.getErrorCount();
	}
	
	public Scanner getScanner()
	{
		return scanner;
	}

}
