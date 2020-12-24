package wci.frontend.pascal.parsers;

import static wci.frontend.pascal.PascalErrorCode.IDENTIFIER_UNDEFINED;
import static wci.frontend.pascal.PascalErrorCode.INCOMPATIBLE_TYPES;
import static wci.frontend.pascal.PascalErrorCode.MISSING_RIGHT_PAREN;
import static wci.frontend.pascal.PascalErrorCode.UNEXPECTED_TOKEN;
import static wci.frontend.pascal.PascalTokenType.DIV;
import static wci.frontend.pascal.PascalTokenType.EQUALS;
import static wci.frontend.pascal.PascalTokenType.GREATER_EQUALS;
import static wci.frontend.pascal.PascalTokenType.GREATER_THAN;
import static wci.frontend.pascal.PascalTokenType.IDENTIFIER;
import static wci.frontend.pascal.PascalTokenType.INTEGER;
import static wci.frontend.pascal.PascalTokenType.LEFT_PAREN;
import static wci.frontend.pascal.PascalTokenType.LESS_EQUALS;
import static wci.frontend.pascal.PascalTokenType.LESS_THAN;
import static wci.frontend.pascal.PascalTokenType.MINUS;
import static wci.frontend.pascal.PascalTokenType.NOT_EQUALS;
import static wci.frontend.pascal.PascalTokenType.PLUS;
import static wci.frontend.pascal.PascalTokenType.REAL;
import static wci.frontend.pascal.PascalTokenType.RIGHT_PAREN;
import static wci.frontend.pascal.PascalTokenType.SLASH;
import static wci.frontend.pascal.PascalTokenType.STAR;
import static wci.frontend.pascal.PascalTokenType.STRING;
import static wci.intermediate.icodeimpl.ICodeKeyImpl.VALUE;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.ADD;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.EQ;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.FLOAT_DIVIDE;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.GE;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.GT;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.INTEGER_CONSTANT;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.INTEGER_DIVIDE;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.LE;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.LT;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.MULTIPLY;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.NE;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.NEGATE;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.REAL_CONSTANT;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.STRING_CONSTANT;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.SUBTRACT;
import static wci.intermediate.symtabimpl.DefinitionImpl.UNDEFINED;
import static wci.intermediate.symtabimpl.SymTabKeyImpl.CONSTANT_VALUE;



import java.util.EnumSet;
import java.util.HashMap;

import wci.frontend.Token;
import wci.frontend.TokenType;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.Definition;
import wci.intermediate.ICodeFactory;
import wci.intermediate.ICodeNode;
import wci.intermediate.ICodeNodeType;
import wci.intermediate.SymTabEntry;
import wci.intermediate.TypeFactory;
import wci.intermediate.TypeSpec;
import wci.intermediate.icodeimpl.ICodeNodeTypeImpl;
import wci.intermediate.symtabimpl.DefinitionImpl;
import wci.intermediate.symtabimpl.Predefined;
import wci.intermediate.typeimpl.TypeChecker;

public class ExpressionParser extends StatementParser 
{
	static final EnumSet<PascalTokenType> EXPR_START_SET =
			EnumSet.of(PLUS, MINUS, IDENTIFIER, INTEGER, REAL, STRING, 
					PascalTokenType.NOT, LEFT_PAREN);

	public ExpressionParser(PascalParserTD parent) 
	{
		super(parent);
	}
	
	public ICodeNode parse(Token token) 
		throws Exception
	{
		return parseExpression(token);
	}
	
	private static final EnumSet<PascalTokenType> REL_OPS =
			EnumSet.of(EQUALS, NOT_EQUALS, LESS_THAN, LESS_EQUALS,
					GREATER_THAN, GREATER_EQUALS);
	private static final HashMap<PascalTokenType, ICodeNodeType> 
		REL_OPS_MAP = new HashMap<PascalTokenType, ICodeNodeType>();
	static 
	{
		REL_OPS_MAP.put(EQUALS, EQ);
		REL_OPS_MAP.put(NOT_EQUALS, NE);
		REL_OPS_MAP.put(LESS_THAN, LT);
		REL_OPS_MAP.put(LESS_EQUALS, LE);
		REL_OPS_MAP.put(GREATER_THAN, GT);
		REL_OPS_MAP.put(GREATER_EQUALS, GE);
	};
	
	private static final EnumSet<PascalTokenType> ADD_OPS =
			EnumSet.of(PLUS, MINUS, PascalTokenType.OR);
	
	private static final HashMap<PascalTokenType, ICodeNodeType> 
		ADD_OPS_OPS_MAP = new HashMap<PascalTokenType, ICodeNodeType>();
	static 
	{
		ADD_OPS_OPS_MAP.put(PLUS, ADD);
		ADD_OPS_OPS_MAP.put(MINUS, SUBTRACT);
		ADD_OPS_OPS_MAP.put(PascalTokenType.OR, ICodeNodeTypeImpl.OR);
	};

	private static final EnumSet<PascalTokenType> MULT_OPS = 
			EnumSet.of(STAR, SLASH, DIV, PascalTokenType.MOD, PascalTokenType.AND);
	
	private static final HashMap<PascalTokenType, ICodeNodeType> MULT_OPS_OPS_MAP =
			new HashMap<PascalTokenType, ICodeNodeType>();
	static {
		MULT_OPS_OPS_MAP.put(STAR, MULTIPLY);
		MULT_OPS_OPS_MAP.put(SLASH, FLOAT_DIVIDE);
		MULT_OPS_OPS_MAP.put(DIV, INTEGER_DIVIDE);
		MULT_OPS_OPS_MAP.put(PascalTokenType.MOD, ICodeNodeTypeImpl.MOD);
		MULT_OPS_OPS_MAP.put(PascalTokenType.AND, ICodeNodeTypeImpl.AND);
	};
	
	private ICodeNode parseExpression(Token token)
		throws Exception
	{
		ICodeNode rootNode = parseSimpleExpression(token);
		TypeSpec resultType = rootNode != null ? rootNode.getTypeSpec()
											  : Predefined.undefinedType;
		
		token = currentToken();
		TokenType tokenType = token.getType();
		
		if (REL_OPS.contains(tokenType))
		{
			ICodeNodeType nodeType = REL_OPS_MAP.get(tokenType);
			ICodeNode opNode = ICodeFactory.createICodeNode(nodeType);
			opNode.addChild(rootNode);
			
			token = nextToken();
			ICodeNode simpExprNode = parseSimpleExpression(token);
			opNode.addChild(simpExprNode);
			rootNode = opNode;
			
			TypeSpec simpExprType = simpExprNode != null ? simpExprNode.getTypeSpec() 
														: Predefined.undefinedType;
			
			if (TypeChecker.areComparisonCompatible(resultType, simpExprType)) {
				resultType = Predefined.booleanType;
			}
			
			else {
				errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
				resultType = Predefined.undefinedType;
			}
			
		}
		
		if (rootNode != null) {
			rootNode.setTypeSpec(resultType);
		}
		
		return rootNode;
	}
		
	private ICodeNode parseSimpleExpression(Token token)
		throws Exception
	{
		Token signToken = null;
		TokenType signType = null;
		
		TokenType tokenType = token.getType();
		if ((tokenType == PLUS) || (tokenType == MINUS))
		{
			signType = tokenType;
			signToken = token;
			token = nextToken();
		}
		
		ICodeNode rootNode = parseTerm(token);
		TypeSpec resultType = rootNode != null ? rootNode.getTypeSpec() 
											  : Predefined.undefinedType;
		
		if ((signType != null) && (!TypeChecker.isIntegerOrReal(resultType))) {
			errorHandler.flag(signToken, INCOMPATIBLE_TYPES, this);
		}

		
		if (signType == MINUS)
		{
			ICodeNode negateNode = ICodeFactory.createICodeNode(NEGATE);
			negateNode.addChild(rootNode);
			negateNode.setTypeSpec(rootNode.getTypeSpec());
			rootNode = negateNode;
		}
		
		token = currentToken();
		tokenType = token.getType();
		
		while (ADD_OPS.contains(tokenType))
		{
			TokenType operator = tokenType;
			ICodeNodeType nodeType = ADD_OPS_OPS_MAP.get(operator);
			ICodeNode opNode = ICodeFactory.createICodeNode(nodeType);
			opNode.addChild(rootNode);
			
			token = nextToken();
			ICodeNode termNode = parseTerm(token);
			opNode.addChild(termNode);
			TypeSpec termType = termNode != null ? termNode.getTypeSpec()
												: Predefined.undefinedType;
			rootNode = opNode;
			
			switch ((PascalTokenType) operator) {
			
				case PLUS:
				case MINUS: {
					if (TypeChecker.areBothInteger(resultType, termType)) {
						resultType = Predefined.integerType;
					}
				
					else if (TypeChecker.areBothNumbersAndAtLeastOneReal(resultType, termType)) {
						resultType = Predefined.realType;
					}
					
					else {
						errorHandler.flag(token, INCOMPATIBLE_TYPES, this);						
					}
					
					break;
				}
				case OR: {
					
					if (TypeChecker.areBothBoolean(resultType, termType)) {
						resultType = Predefined.booleanType;
					}
					
					else {
						errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
					}
					
					break;
					
				}
			}
			
			rootNode.setTypeSpec(resultType);
			token = currentToken();
			tokenType = token.getType();
		}
		
		return rootNode;
	}
			
	private ICodeNode parseTerm(Token token)
		throws Exception
	{

		ICodeNode rootNode = parseFactor(token);
		TypeSpec resultType = rootNode != null ? rootNode.getTypeSpec() 
				  							  : Predefined.undefinedType;

		token = currentToken();
		TokenType tokenType = token.getType();
		
		while (MULT_OPS.contains(tokenType))
		{
			TokenType operator = tokenType;
			ICodeNodeType nodeType = MULT_OPS_OPS_MAP.get(operator);
			ICodeNode opNode = ICodeFactory.createICodeNode(nodeType);
			opNode.addChild(rootNode);
			
			token = nextToken();
			ICodeNode factorNode = parseFactor(token);
			opNode.addChild(factorNode);
			TypeSpec factorType = factorNode != null ? factorNode.getTypeSpec()
												 	: Predefined.undefinedType;
			rootNode = opNode;
			
			switch ((PascalTokenType) operator) {
			
				case STAR: { 
					
					if (TypeChecker.areBothInteger(resultType, factorType)) {
						resultType = Predefined.integerType;
					}
					
					else if (TypeChecker.areBothNumbersAndAtLeastOneReal(resultType, factorType)) {
						resultType = Predefined.realType;
					}
					
					else {
						errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
					}
					
					break;
					
				}
				
				case SLASH: {
					
					if (TypeChecker.areBothInteger(resultType, factorType) || 
						TypeChecker.areBothNumbersAndAtLeastOneReal(resultType, factorType)) {
						
						resultType = Predefined.realType;
					}
					
					else {
						errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
					}
					
					break;
				}
				
				case DIV:
				case MOD: {
					
					if (TypeChecker.areBothInteger(resultType, factorType)) {
						resultType = Predefined.integerType;
					}
					
					else {
						errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
					}
				}
				
				case AND: {
					
					if (TypeChecker.areBothBoolean(resultType, factorType)) {
						resultType = Predefined.booleanType;
					}
					
					else {
						errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
					}
					
					break;
					
				}
			
			}
			
			rootNode.setTypeSpec(resultType);
			
			token = currentToken();
			tokenType = token.getType();
		}
		
		return rootNode;
	}
	
	private ICodeNode parseFactor(Token token)
		throws Exception
	{
		TokenType tokenType = token.getType();
		ICodeNode rootNode = null;
		
		switch ((PascalTokenType) tokenType)
		{
			case IDENTIFIER:
				{
					return parseIdentifier(token);
				}
				
			case INTEGER:
			{

				rootNode = ICodeFactory.createICodeNode(INTEGER_CONSTANT);
				rootNode.setAttribute(VALUE, token.getValue());
				
				token = nextToken();
				rootNode.setTypeSpec(Predefined.integerType);
				
				break;
			}
			
			case REAL:
			{

				rootNode = ICodeFactory.createICodeNode(REAL_CONSTANT);
				rootNode.setAttribute(VALUE, token.getValue());
				
				token = nextToken();
				rootNode.setTypeSpec(Predefined.realType);
				
				break;
			}
			
			case STRING:
			{
				String value = (String) token.getValue();
				
				rootNode = ICodeFactory.createICodeNode(STRING_CONSTANT);
				rootNode.setAttribute(VALUE, value);
				
				TypeSpec resultType = value.length() == 1 ? Predefined.charType
														 : TypeFactory.createStringType(value);
				token = nextToken();				
				rootNode.setTypeSpec(resultType);
				
				break;
			}
			
			case NOT:
			{
				token = nextToken();
				rootNode = ICodeFactory.createICodeNode(ICodeNodeTypeImpl.NOT);
				ICodeNode factorNode = parseFactor(token);
				rootNode.addChild(factorNode);
				
				TypeSpec factorType = factorNode != null ? factorNode	.getTypeSpec()
														: Predefined.undefinedType;
				
				if (!TypeChecker.isBoolean(factorType)) {
					errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
				}
				
				rootNode.setTypeSpec(Predefined.booleanType);
				break;
			}
			
			case LEFT_PAREN:
			{
				token = nextToken();
				
				rootNode = parseExpression(token);
				token = currentToken();
				if (token.getType() == RIGHT_PAREN)
				{
					token = nextToken();
				}
				else
				{
					errorHandler.flag(token, MISSING_RIGHT_PAREN, this);
				}
				
				break;
			}
			
			default:
			{
				errorHandler.flag(token, UNEXPECTED_TOKEN, this);
			}
			
		}
		
		return rootNode;
	}
	
	private ICodeNode parseIdentifier(Token token)
		throws Exception{
		
		ICodeNode rootNode = null;
		
		// Check that the identifier is defined locally
		String name = token.getText().toLowerCase();
		SymTabEntry id = symTabStack.lookup(name);
		
		// Undefined
		if (id == null) {
			errorHandler.flag(token, IDENTIFIER_UNDEFINED, this);
			id = symTabStack.enterLocal(name);
			id.setDefinition(UNDEFINED);
			id.setTypeSpec(Predefined.undefinedType);
		}
		
		Definition defnCode = id.getDefinition();
		
		switch ((DefinitionImpl) defnCode) {
		
			case CONSTANT: {
				Object value = id.getAttribute(CONSTANT_VALUE);
				TypeSpec type = id.getTypeSpec();
				
				if (value instanceof Integer) {
					rootNode = ICodeFactory.createICodeNode(INTEGER_CONSTANT);
					rootNode.setAttribute(VALUE, value);
				}
				else if (value instanceof Float) {
					rootNode = ICodeFactory.createICodeNode(REAL_CONSTANT);
					rootNode.setAttribute(VALUE, value);
				}
				else if (value instanceof String) {
					rootNode = ICodeFactory.createICodeNode(STRING_CONSTANT);
					rootNode.setAttribute(VALUE, value);
				}
				
				id.appendLineNumber(token.getLineNumber());
				token = nextToken();
				
				if (rootNode != null) {
					rootNode.setTypeSpec(type);
				}
				
				break;
			}
			
			case ENUMERATION_CONSTANT: {
				Object value = id.getAttribute(CONSTANT_VALUE);
				TypeSpec type = id.getTypeSpec();
				
				rootNode = ICodeFactory.createICodeNode(INTEGER_CONSTANT);
				rootNode.setAttribute(VALUE, value);
				
				id.appendLineNumber(token.getLineNumber());
				token = nextToken();
				rootNode.setTypeSpec(type);
				break;
			}

			case FUNCTION: {
				CallParser callParser = new CallParser(this);
				rootNode = callParser.parse(token);
				break;
			}
			
			default: {
				VariableParser variableParser = new VariableParser(this);
				rootNode = variableParser.parse(token, id);
				break;
			}
		}
		
		return rootNode;
	}

}
