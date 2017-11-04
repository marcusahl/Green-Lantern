package wci.frontend.pascal.parsers;

import wci.frontend.Token;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.ICodeFactory;
import wci.intermediate.ICodeNode;
import wci.intermediate.icodeimpl.ICodeNodeTypeImpl;

import java.util.EnumSet;

import static wci.frontend.pascal.PascalTokenType.*;
import static wci.frontend.pascal.PascalErrorCode.*;

public class IfStatementParser extends StatementParser {
	
	private static final EnumSet<PascalTokenType> THEN_SET = StatementParser.STMT_START_SET.clone();
	static {
		THEN_SET.add(THEN);
		THEN_SET.addAll(StatementParser.STMT_FOLLOW_SET);
	}

	public IfStatementParser(PascalParserTD parent) {
		super(parent);

	}
	
	public ICodeNode parse(Token token) throws Exception {
		token = nextToken();
		
		ICodeNode ifNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.IF);
		
		ExpressionParser expressionParser = new ExpressionParser(this);
		ifNode.addChild(expressionParser.parse(token));
		
		token = synchronize(THEN_SET);
		if (token.getType() == THEN) {
			token = nextToken();
		}
		else {
			errorHandler.flag(token, MISSING_THEN, this);
		}
		
		StatementParser statementParser = new StatementParser(this);
		ifNode.addChild(statementParser.parse(token));
		token = currentToken();
		
		if (token.getType() == ELSE) {
			token = nextToken();
			
			ifNode.addChild(statementParser.parse(token));
		}
		
		return ifNode;
	}

}
