package wci.frontend.pascal.parsers;

import static wci.frontend.pascal.PascalErrorCode.INCOMPATIBLE_TYPES;
import static wci.frontend.pascal.PascalErrorCode.INVALID_SUBRANGE_TYPE;
import static wci.frontend.pascal.PascalErrorCode.MIN_GT_MAX;
import static wci.frontend.pascal.PascalErrorCode.MISSING_DOT_DOT;
import static wci.frontend.pascal.PascalTokenType.DOT_DOT;
import static wci.frontend.pascal.PascalTokenType.IDENTIFIER;
import static wci.intermediate.typeimpl.TypeFormImpl.ENUMERATION;
import static wci.intermediate.typeimpl.TypeFormImpl.SUBRANGE;
import static wci.intermediate.typeimpl.TypeKeyImpl.*;

import wci.frontend.Scanner;
import wci.frontend.Token;
import wci.frontend.TokenType;
import wci.frontend.pascal.PascalParserTD;
import wci.intermediate.TypeFactory;
import wci.intermediate.TypeSpec;
import wci.intermediate.symtabimpl.Predefined;

public class SubrangeTypeParser extends TypeSpecificationParser {
	
	public TypeSpec parse(Token token) 
		throws Exception
	{
		TypeSpec subrangeType = TypeFactory.createType(SUBRANGE);
		Object minValue = null;
		Object maxValue = null;
		
		// Parse the minimum constant
		Token constantToken = token;
		ConstantDefinitionsParser constantParser = new ConstantDefinitionsParser(this);
		minValue = constantParser.parseConstant(token);
		
		TypeSpec minType = constantToken.getType() == IDENTIFIER
						 ? constantParser.getConstantType(constantToken)
						 : constantParser.getConstantType(minValue);
		
		minValue = checkValueType(constantToken, minValue, minType);
		
		token = currentToken();
		Boolean sawDotDot = false;
		
		if (token.getType() == DOT_DOT) {
			token = nextToken();
			sawDotDot = true;
		}
		
		TokenType tokenType = token.getType();
		
		if (ConstantDefinitionsParser.CONSTANT_START_SET.contains(tokenType)) {
			if (!sawDotDot) {
				errorHandler.flag(token, MISSING_DOT_DOT, this);
			}
			
			// Parse the maximum constant
			token = synchronize(ConstantDefinitionsParser.CONSTANT_START_SET);
			constantToken = token;
			maxValue = constantParser.parseConstant(token);

			TypeSpec maxType = constantToken.getType() == IDENTIFIER
							 ? constantParser.getConstantType(constantToken)
							 : constantParser.getConstantType(maxValue);
			
			maxValue = checkValueType(constantToken, maxValue, maxType);
			
			if ((minType == null) || (maxType == null)) {
				errorHandler.flag(constantToken, INCOMPATIBLE_TYPES, this);
			}
			
			else if ((minValue != null) && (maxValue != null) && 
					((Integer) minValue >= (Integer) maxValue)) {
						errorHandler.flag(constantToken, MIN_GT_MAX, this);
					}

		}
		else {
			errorHandler.flag(constantToken, INVALID_SUBRANGE_TYPE, this);
		}
		
		subrangeType.setAttribute(SUBRANGE_BASE_TYPE, minType);
		subrangeType.setAttribute(SUBRANGE_MIN_VALUE, minValue);
		subrangeType.setAttribute(SUBRANGE_MAX_VALUE, maxValue);
		
		return subrangeType;
		
	}
	
	public Object checkValueType(Token token, Object value, TypeSpec type) {
		
		if (type == null) {
			return value;
		}
		
		if (type == Predefined.integerType) {
			return value;
		}
		
		else if (type == Predefined.charType) {
			char ch = ((String) value).charAt(0);
			return Character.getNumericValue(ch);
		}
		
		else if (type.getForm() == ENUMERATION) {
			return value;
		}
		
		else {
			errorHandler.flag(token, INVALID_SUBRANGE_TYPE, this);
			return value;
		}
	}

	public SubrangeTypeParser(Scanner scanner) {
		super(scanner);
	}

	public SubrangeTypeParser(PascalParserTD parent) {
		super(parent);
	}

}
