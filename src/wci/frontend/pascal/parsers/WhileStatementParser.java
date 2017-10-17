package wci.frontend.pascal.parsers;

import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.*;
import static wci.frontend.pascal.PascalTokenType.*;
import static wci.frontend.pascal.PascalErrorCode.*;

import java.util.EnumSet;

import wci.frontend.Token;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.ICodeFactory;
import wci.intermediate.ICodeNode;
import wci.intermediate.icodeimpl.ICodeNodeTypeImpl;

public class WhileStatementParser extends StatementParser {
	
	// SynchrEnumSet<Enum<E>>on set for DO.
	private static final EnumSet<PascalTokenType> DO_SET = 
			StatementParser.STMT_START_SET.clone();
	static {
		DO_SET.add(DO);
		DO_SET.addAll(StatementParser.STMT_FOLLOW_SET);
	}
	
	public WhileStatementParser(PascalParserTD parent) {
		super(parent);
	}
	
	public ICodeNode parse(Token token) throws Exception {
		
		// consume the WHILE
		token = nextToken();
		
		// Create LOOP, TEST, and NOT nodes.
		ICodeNode loopNode = ICodeFactory.createICodeNode(LOOP);
		ICodeNode breakNode = ICodeFactory.createICodeNode(TEST);
		ICodeNode notNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.NOT);
		
		// The LOOP node adopts the TEST node as its first child.
		// The TEST node adopts the NOT node as its only child.
		loopNode.addChild(breakNode);
		breakNode.addChild(notNode);
		
		// Parse the expression.
		// The NOT node adopts the expression subtree as its only child
		ExpressionParser expressionParser = new ExpressionParser(this);
		notNode.addChild(expressionParser.parse(token));
		
		// Synchronize at the DO
		token = synchronize(DO_SET);
		if (token.getType() == DO) {
			
			// consume the DO
			token = nextToken();
		}
		
		else {
			errorHandler.flag(token, MISSING_DO, this);
		}
		
		// Parse the statement.
		// The LOOP node adopts the statement subtree as its second child.
		StatementParser statementParser = new StatementParser(this);
		loopNode.addChild(statementParser.parse(token));
		
		return loopNode;
		
	}

}
