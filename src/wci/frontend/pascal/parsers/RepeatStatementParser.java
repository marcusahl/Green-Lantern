package wci.frontend.pascal.parsers;

import wci.frontend.Token;
import wci.frontend.pascal.PascalParserTD;
import wci.intermediate.ICodeFactory;
import wci.intermediate.ICodeNode;

import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.*;
import static wci.frontend.pascal.PascalTokenType.*;
import static wci.frontend.pascal.PascalErrorCode.*;

public class RepeatStatementParser extends StatementParser {

	public RepeatStatementParser(PascalParserTD parent) {
		super(parent);
	}
	
	public ICodeNode parse(Token token)
		throws Exception {
		
		// Consume the REPEAT
		token = nextToken();  
		
		// Create the LOOP and TEST nodes.
		ICodeNode loopNode = ICodeFactory.createICodeNode(LOOP);
		ICodeNode testNode = ICodeFactory.createICodeNode(TEST);
		
		// Parse the statement list terminated by the UNTIL token
		// The LOOP node is the parent of the statement subtree
		StatementParser statementParser = new StatementParser(this);
		statementParser.parseList(token, loopNode, UNTIL, MISSING_UNTIL);
		token = currentToken();
		
		// Parse the expression.
		// The TEST node adopts the expression subtree as its only child
		// The LOOP node adopts the TEST node
		ExpressionParser expressionParser = new ExpressionParser(this);
		testNode.addChild(expressionParser.parse(token));
		loopNode.addChild(testNode);
		
		return loopNode;
		
	}

}
