package wci.frontend.pascal;

import static wci.frontend.pascal.PascalErrorCode.IO_ERROR;
import static wci.frontend.pascal.PascalErrorCode.MISSING_PERIOD;
import static wci.frontend.pascal.PascalErrorCode.UNEXPECTED_TOKEN;
import static wci.frontend.pascal.PascalTokenType.BEGIN;
import static wci.frontend.pascal.PascalTokenType.DOT;
import static wci.message.MessageType.PARSER_SUMMARY;

import java.util.EnumSet;

import wci.frontend.EofToken;
import wci.frontend.Parser;
import wci.frontend.Scanner;
import wci.frontend.Token;
import wci.frontend.pascal.parsers.StatementParser;
import wci.intermediate.ICodeFactory;
import wci.intermediate.ICodeNode;
import wci.message.Message;

public class PascalParserTD extends Parser {
	
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
		iCode = ICodeFactory.createICode();
		
		try
		{
			Token token = nextToken();
			ICodeNode rootNode = null;
			
			
			//	Look for the BEGIN token to parse a compound statement.
			if (token.getType() == BEGIN)
			{
				StatementParser statementParser = new StatementParser(this);
				rootNode = statementParser.parse(token);
				token = currentToken();
			}
			else 
			{
				errorHandler.flag(token, UNEXPECTED_TOKEN, this);
			}
			
			// Look for the final period.
			if (token.getType() != DOT)
			{
				errorHandler.flag(token, MISSING_PERIOD, this);
			}
			token = currentToken();
			
			//	Set the parse tree root node.
			if (rootNode != null)
			{
				iCode.setRoot(rootNode);
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

	public Token synchronize(EnumSet syncSet) 
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
