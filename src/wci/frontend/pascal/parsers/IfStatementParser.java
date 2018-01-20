package wci.frontend.pascal.parsers;

import static wci.frontend.pascal.PascalErrorCode.INCOMPATIBLE_TYPES;
import static wci.frontend.pascal.PascalErrorCode.MISSING_THEN;
import static wci.frontend.pascal.PascalTokenType.ELSE;
import static wci.frontend.pascal.PascalTokenType.THEN;

import java.util.EnumSet;

import wci.frontend.Token;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.ICodeFactory;
import wci.intermediate.ICodeNode;
import wci.intermediate.TypeSpec;
import wci.intermediate.icodeimpl.ICodeNodeTypeImpl;
import wci.intermediate.symtabimpl.Predefined;
import wci.intermediate.typeimpl.TypeChecker;

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
		ICodeNode exprNode = expressionParser.parse(token);
		ifNode.addChild(exprNode);
		
		TypeSpec exprType = exprNode != null ? exprNode.getTypeSpec()
											: Predefined.undefinedType;
		if (!TypeChecker.isBoolean(exprType)) {
			errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
		}
		
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
