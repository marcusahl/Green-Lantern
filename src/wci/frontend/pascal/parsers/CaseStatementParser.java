package wci.frontend.pascal.parsers;

import static wci.frontend.pascal.PascalErrorCode.CASE_CONSTANT_REUSED;
import static wci.frontend.pascal.PascalErrorCode.INVALID_CONSTANT;
import static wci.frontend.pascal.PascalErrorCode.MISSING_COLON;
import static wci.frontend.pascal.PascalErrorCode.MISSING_COMMA;
import static wci.frontend.pascal.PascalErrorCode.MISSING_END;
import static wci.frontend.pascal.PascalErrorCode.MISSING_OF;
import static wci.frontend.pascal.PascalErrorCode.MISSING_SEMICOLON;
import static wci.frontend.pascal.PascalTokenType.COLON;
import static wci.frontend.pascal.PascalTokenType.COMMA;
import static wci.frontend.pascal.PascalTokenType.END;
import static wci.frontend.pascal.PascalTokenType.IDENTIFIER;
import static wci.frontend.pascal.PascalTokenType.INTEGER;
import static wci.frontend.pascal.PascalTokenType.MINUS;
import static wci.frontend.pascal.PascalTokenType.OF;
import static wci.frontend.pascal.PascalTokenType.PLUS;
import static wci.frontend.pascal.PascalTokenType.STRING;
import static wci.frontend.pascal.PascalTokenType.SEMICOLON;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.INTEGER_CONSTANT;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.SELECT;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.SELECT_BRANCH;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.SELECT_CONSTANTS;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.STRING_CONSTANT;
import static wci.intermediate.icodeimpl.ICodeKeyImpl.VALUE;

import java.util.EnumSet;
import java.util.HashSet;

import wci.frontend.EofToken;
import wci.frontend.Token;
import wci.frontend.TokenType;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.ICodeFactory;
import wci.intermediate.ICodeNode;

public class CaseStatementParser extends StatementParser {
	
	private static final EnumSet<PascalTokenType> CONSTANT_START_SET = 
			EnumSet.of(IDENTIFIER, INTEGER, PLUS, MINUS, STRING);	
	private static final EnumSet<PascalTokenType> OF_SET =
			CONSTANT_START_SET.clone();
	static {
		OF_SET.add(OF);
		OF_SET.addAll(StatementParser.STMT_FOLLOW_SET);
	}
	private static final EnumSet<PascalTokenType> COMMA_SET =
			CONSTANT_START_SET.clone();
	static {
		COMMA_SET.add(COMMA);
		COMMA_SET.add(COLON);
		COMMA_SET.addAll(StatementParser.STMT_START_SET);
		COMMA_SET.addAll(StatementParser.STMT_FOLLOW_SET);
	}

	public CaseStatementParser(PascalParserTD parent) {
		super(parent);
	}
	
	public ICodeNode parse(Token token) throws Exception {
		token = nextToken();
		
		ICodeNode selectNode = ICodeFactory.createICodeNode(SELECT);
		
		// The SELECT node adopts the expression subtree as its first child
		ExpressionParser expressionParser = new ExpressionParser(this);
		selectNode.addChild(expressionParser.parse(token));
		
		// Synchronize at the OF.
		token = synchronize(OF_SET);
		if(token.getType() == OF) {
			token = nextToken();
		}
		else {
			errorHandler.flag(token, MISSING_OF, this);
		}
		
		// Set of CASE branch constants
		HashSet<Object> constantSet = new HashSet<Object>();
		
		// Loop to parse each CASE branch until the END token
		// or the end of the source file.
		while (!(token instanceof EofToken) && (token.getType() != END)) {
			
			// The SELECT node adopts the CASE branch subtree.
			selectNode.addChild(parseBranch(token, constantSet));
			
			token = currentToken();
			TokenType tokenType = token.getType();
			
			if (tokenType == SEMICOLON) {
				token = nextToken(); 
			}
			else if (CONSTANT_START_SET.contains(tokenType)) {
				errorHandler.flag(token, MISSING_SEMICOLON,  this);
			}
		}
		
		if (token.getType() == END) {
			token = nextToken();
		}
		else {
			errorHandler.flag(token, MISSING_END, this);
		}
		
		return selectNode;
	}
	
	private ICodeNode parseBranch(Token token, HashSet<Object> constantSet) throws Exception {
		
		ICodeNode branchNode = ICodeFactory.createICodeNode(SELECT_BRANCH);
		ICodeNode constantNode = ICodeFactory.createICodeNode(SELECT_CONSTANTS);
		branchNode.addChild(constantNode);
		
		parseConstantList(token, constantNode, constantSet);
		
		token = currentToken();
		if (token.getType() == COLON) {
			token = nextToken();
		}
		else {
			errorHandler.flag(token, MISSING_COLON, this);
		}
		
		StatementParser statementParser = new StatementParser(this);
		branchNode.addChild(statementParser.parse(token));
		
		return branchNode;
	
	}
	
	private void parseConstantList(Token token, ICodeNode constantNode, HashSet<Object> constantSet) 
		throws Exception {
		
		while (CONSTANT_START_SET.contains(token.getType())) {
			
			constantNode.addChild(parseConstant(token, constantSet));
			
			token = synchronize(COMMA_SET);
			
			if (token.getType() == COMMA) {
				token = nextToken();
			}
			else if (CONSTANT_START_SET.contains(token.getType())) {
				errorHandler.flag(token, MISSING_COMMA, this);
			}
		}
	}
	
	private ICodeNode parseConstant(Token token, HashSet<Object> constantSet) throws Exception {
		
		TokenType sign = null;
		ICodeNode constantNode = null;
		
		// Synchronize at the start of a constant.
		token = synchronize(CONSTANT_START_SET);
		TokenType tokenType = token.getType();
		
		if (tokenType == PLUS || tokenType == MINUS) {
			sign = tokenType;
			token = nextToken();
		}
		
		switch ((PascalTokenType) token.getType()) {
		
			case IDENTIFIER: {
				constantNode = parseIdentifierConstant(token, sign);
				break;
			}
			
			case INTEGER: {
				constantNode = parseIntegerConstant(token, sign);
				break;
			}
			
			case STRING: {
				constantNode = parseCharacterConstant(token, sign);
				break;
			}
			
			default: {
				errorHandler.flag(token, INVALID_CONSTANT, this);
				break;
			}
		}
		
		if (constantNode != null) {
			Object value = constantNode.getAttribute(VALUE);
			
			if (constantSet.contains(value)) {
				errorHandler.flag(token, CASE_CONSTANT_REUSED, this);
			}
			else {
				constantSet.add(value);
			}
		}
		
		nextToken();
		return constantNode;
	}
	
	private ICodeNode parseIdentifierConstant(Token token, TokenType sign) throws Exception {
		// TODO Implement this. For now, just throw an error if code reaches here
		errorHandler.flag(token, INVALID_CONSTANT, this);
		return null;
	}
	
	private ICodeNode parseIntegerConstant(Token token, TokenType sign) throws Exception {
		
		ICodeNode constantNode = ICodeFactory.createICodeNode(INTEGER_CONSTANT);
		int value = Integer.parseInt(token.getText());
		
		if (sign == MINUS) {
			value = -value;
		}
		
		constantNode.setAttribute(VALUE, value);
		return constantNode;
	}
	
	private ICodeNode parseCharacterConstant(Token token, TokenType sign) throws Exception {
		
		ICodeNode constantNode = null;
		String value = (String) token.getValue();
		
		if (sign != null) {
			errorHandler.flag(token, INVALID_CONSTANT, this);
		}
		else {
			if (value.length() == 1) {
				constantNode = ICodeFactory.createICodeNode(STRING_CONSTANT);
				constantNode.setAttribute(VALUE, value);
			}
			else {
				errorHandler.flag(token, INVALID_CONSTANT, this);
			}
		}
		
		return constantNode;

	}

}
