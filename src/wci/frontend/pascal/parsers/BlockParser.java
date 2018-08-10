package wci.frontend.pascal.parsers;

import wci.frontend.Scanner;
import wci.frontend.Token;
import wci.frontend.TokenType;
import wci.frontend.pascal.PascalParserTD;
import wci.intermediate.ICodeFactory;
import wci.intermediate.ICodeNode;
import wci.intermediate.SymTabEntry;

import static wci.frontend.pascal.PascalTokenType.BEGIN;
import static wci.frontend.pascal.PascalTokenType.END;
import static wci.frontend.pascal.PascalErrorCode.MISSING_BEGIN;
import static wci.frontend.pascal.PascalErrorCode.MISSING_END;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.COMPOUND;

public class BlockParser extends PascalParserTD {

	public BlockParser(Scanner scanner) {
		super(scanner);
	}

	public BlockParser(PascalParserTD parent) {
		super(parent);
	}
	
	public ICodeNode parse(Token token, SymTabEntry routineId) 
		throws Exception 
	{
		DeclarationsParser declarationParser = new DeclarationsParser(this);
		StatementParser statementParser = new StatementParser(this);
		declarationParser.parse(token, routineId);
		token = synchronize(StatementParser.STMT_START_SET);
		TokenType tokenType = token.getType();
		ICodeNode rootNode = null;
		
		if (tokenType == BEGIN) {
			rootNode = statementParser.parse(token);
		} else {
			errorHandler.flag(token, MISSING_BEGIN, this);
			// attempt to Parse anyway, if possible
			if (StatementParser.STMT_START_SET.contains(tokenType)) {
				rootNode = ICodeFactory.createICodeNode(COMPOUND);
				statementParser.parseList(token, rootNode, END, MISSING_END);
			}
		}
		return rootNode;
	}

}
