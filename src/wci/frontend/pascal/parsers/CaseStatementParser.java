package wci.frontend.pascal.parsers;

import static wci.frontend.pascal.PascalErrorCode.CASE_CONSTANT_REUSED;
import static wci.frontend.pascal.PascalErrorCode.IDENTIFIER_UNDEFINED;
import static wci.frontend.pascal.PascalErrorCode.INCOMPATIBLE_TYPES;
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
import static wci.frontend.pascal.PascalTokenType.SEMICOLON;
import static wci.frontend.pascal.PascalTokenType.STRING;
import static wci.intermediate.icodeimpl.ICodeKeyImpl.VALUE;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.INTEGER_CONSTANT;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.SELECT;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.SELECT_BRANCH;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.SELECT_CONSTANTS;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.STRING_CONSTANT;
import static wci.intermediate.symtabimpl.DefinitionImpl.CONSTANT;
import static wci.intermediate.symtabimpl.DefinitionImpl.ENUMERATION_CONSTANT;
import static wci.intermediate.symtabimpl.DefinitionImpl.UNDEFINED;
import static wci.intermediate.symtabimpl.SymTabKeyImpl.CONSTANT_VALUE;
import static wci.intermediate.typeimpl.TypeFormImpl.ENUMERATION;

import java.util.EnumSet;
import java.util.HashSet;

import wci.frontend.EofToken;
import wci.frontend.Token;
import wci.frontend.TokenType;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.Definition;
import wci.intermediate.ICodeFactory;
import wci.intermediate.ICodeNode;
import wci.intermediate.SymTabEntry;
import wci.intermediate.TypeSpec;
import wci.intermediate.symtabimpl.Predefined;
import wci.intermediate.typeimpl.TypeChecker;

public class CaseStatementParser extends StatementParser {
	
	private static final EnumSet<PascalTokenType> CONSTANT_START_SET = 
			EnumSet.of(IDENTIFIER, INTEGER, PLUS, MINUS, STRING);	
	private static final EnumSet<PascalTokenType> OF_SET =
			CONSTANT_START_SET.clone();
	static {
		OF_SET.add(OF);
		OF_SET.addAll(StatementParser.STMT_FOLLOW_SET);
	}

	public CaseStatementParser(PascalParserTD parent) {
		super(parent);
	}
	
	public ICodeNode parse(Token token) throws Exception {
		token = nextToken();
		
		ICodeNode selectNode = ICodeFactory.createICodeNode(SELECT);
		
		ExpressionParser expressionParser = new ExpressionParser(this);
		ICodeNode exprNode = expressionParser.parse(token);
		selectNode.addChild(exprNode);
		
		TypeSpec exprType = exprNode.getTypeSpec();
		if (!TypeChecker.isInteger(exprType) &&
			!TypeChecker.isChar(exprType) && 
			(exprType.getForm() != ENUMERATION)) 
		{
			errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
		}
		
		token = synchronize(OF_SET);
		if(token.getType() == OF) {
			token = nextToken();
		}
		else {
			errorHandler.flag(token, MISSING_OF, this);
		}
		
		HashSet<Object> constantSet = new HashSet<Object>();
		
		while (!(token instanceof EofToken) && (token.getType() != END)) {
			
			selectNode.addChild(parseBranch(token, constantSet, exprType));
			
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
	
	private ICodeNode parseBranch(Token token, HashSet<Object> constantSet, TypeSpec exprType) 
			throws Exception {
		
		ICodeNode branchNode = ICodeFactory.createICodeNode(SELECT_BRANCH);
		ICodeNode constantNode = ICodeFactory.createICodeNode(SELECT_CONSTANTS);
		branchNode.addChild(constantNode);
		
		parseConstantList(token, constantNode, constantSet, exprType);
		
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
	
	private static final EnumSet<PascalTokenType> COMMA_SET =
			CONSTANT_START_SET.clone();
	static {
		COMMA_SET.add(COMMA);
		COMMA_SET.add(COLON);
		COMMA_SET.addAll(StatementParser.STMT_START_SET);
		COMMA_SET.addAll(StatementParser.STMT_FOLLOW_SET);
	}
	
	private void parseConstantList(Token token, ICodeNode constantNode, HashSet<Object> constantSet, 
			TypeSpec exprType) 
		throws Exception {
		
		while (CONSTANT_START_SET.contains(token.getType())) {
			
			constantNode.addChild(parseConstant(token, constantSet, exprType));
			
			token = synchronize(COMMA_SET);
			
			if (token.getType() == COMMA) {
				token = nextToken();
			}
			else if (CONSTANT_START_SET.contains(token.getType())) {
				errorHandler.flag(token, MISSING_COMMA, this);
			}
		}
	}
	
	private ICodeNode parseConstant(Token token, HashSet<Object> constantSet, TypeSpec exprType) throws Exception {
		
		TokenType sign = null;
		ICodeNode constantNode = null;
		
		token = synchronize(CONSTANT_START_SET);
		TokenType tokenType = token.getType();
		
		if (tokenType == PLUS || tokenType == MINUS) {
			sign = tokenType;
			token = nextToken();
		}
		
		TypeSpec constantType = null;
		switch ((PascalTokenType) token.getType()) {
		
			case IDENTIFIER: {
				constantNode = parseIdentifierConstant(token, sign);
				if (constantNode != null) {
					constantType = constantNode.getTypeSpec();
				}
				break;
			}
			
			case INTEGER: {
				constantNode = parseIntegerConstant(token, sign);
				constantType = Predefined.integerType;
				break;
			}
			
			case STRING: {
				constantNode = parseCharacterConstant(token, sign);
				constantType = Predefined.charType;
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
		
		if (!TypeChecker.areComparisonCompatible(exprType, constantType) ) {
			errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
		}
		
		token = nextToken();
		
//		constantNode.setTypeSpec(constantType);
		return constantNode;
	}
	
	private ICodeNode parseIdentifierConstant(Token token, TokenType sign) 
		throws Exception {
		
		ICodeNode constantNode = null;
		TypeSpec constantType = null;
		
		String name = token.getText().toLowerCase();
		SymTabEntry id = symTabStack.lookup(name);
		
		if (id == null) {
			
			id = symTabStack.enterLocal(name);
			id.setDefinition(UNDEFINED);
			id.setTypeSpec(Predefined.undefinedType);
			errorHandler.flag(token, IDENTIFIER_UNDEFINED, this);
			return null;
		}
		
		Definition defnCode = id.getDefinition();
		
		if ((defnCode == CONSTANT) || (defnCode == ENUMERATION_CONSTANT)) {
			
			Object constantValue = id.getAttribute(CONSTANT_VALUE);
			constantType = id.getTypeSpec();
			
			if ((sign != null) && !TypeChecker.isInteger(constantType)) {
				errorHandler.flag(token, INVALID_CONSTANT, this);
			}
			
			constantNode = ICodeFactory.createICodeNode(INTEGER_CONSTANT);
			constantNode.setAttribute(VALUE, constantValue);
		}
		
		id.appendLineNumber(token.getLineNumber());
		
		if (constantNode != null) {
			constantNode.setTypeSpec(constantType);
		}
		
		return constantNode;
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
