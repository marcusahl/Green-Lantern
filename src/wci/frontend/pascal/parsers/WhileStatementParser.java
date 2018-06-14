package wci.frontend.pascal.parsers;

import static wci.frontend.pascal.PascalErrorCode.INCOMPATIBLE_TYPES;
import static wci.frontend.pascal.PascalErrorCode.MISSING_DO;
import static wci.frontend.pascal.PascalTokenType.DO;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.LOOP;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.TEST;

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

public class WhileStatementParser extends StatementParser {
	
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
		
		token = nextToken();
		
		ICodeNode loopNode = ICodeFactory.createICodeNode(LOOP);
		ICodeNode breakNode = ICodeFactory.createICodeNode(TEST);
		ICodeNode notNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.NOT);
		
		loopNode.addChild(breakNode);
		breakNode.addChild(notNode);
		
		ExpressionParser expressionParser = new ExpressionParser(this);
		ICodeNode exprNode = expressionParser.parse(token);
		notNode.addChild(exprNode);
		
		TypeSpec exprType = exprNode != null ? exprNode.getTypeSpec()
											: Predefined.undefinedType;
		
		if (!TypeChecker.isBoolean(exprType)) {
			errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
		}
		
		token = synchronize(DO_SET);
		if (token.getType() == DO) {
			
			token = nextToken();
		}
		
		else {
			errorHandler.flag(token, MISSING_DO, this);
		}
		
		StatementParser statementParser = new StatementParser(this);
		loopNode.addChild(statementParser.parse(token));
		
		return loopNode;
		
	}

}
