package wci.frontend.pascal.parsers;

import wci.frontend.Token;
import wci.frontend.pascal.PascalParserTD;
import wci.intermediate.ICodeFactory;
import wci.intermediate.ICodeNode;
import wci.intermediate.TypeSpec;
import wci.intermediate.symtabimpl.Predefined;
import wci.intermediate.typeimpl.TypeChecker;

import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.*;
import static wci.frontend.pascal.PascalTokenType.*;
import static wci.frontend.pascal.PascalErrorCode.*;

public class RepeatStatementParser extends StatementParser {

	public RepeatStatementParser(PascalParserTD parent) {
		super(parent);
	}
	
	public ICodeNode parse(Token token)
		throws Exception {
		
		token = nextToken();  
		
		ICodeNode loopNode = ICodeFactory.createICodeNode(LOOP);
		ICodeNode testNode = ICodeFactory.createICodeNode(TEST);
		
		StatementParser statementParser = new StatementParser(this);
		statementParser.parseList(token, loopNode, UNTIL, MISSING_UNTIL);
		token = currentToken();
		
		ExpressionParser expressionParser = new ExpressionParser(this);
		ICodeNode exprNode = expressionParser.parse(token);
		testNode.addChild(exprNode);
		loopNode.addChild(testNode);
		
		TypeSpec exprType = exprNode != null ? exprNode.getTypeSpec()
											: Predefined.undefinedType;
		
		if (!TypeChecker.isBoolean(exprType)) {
			errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
		}
		
		return loopNode;
		
	}

}
