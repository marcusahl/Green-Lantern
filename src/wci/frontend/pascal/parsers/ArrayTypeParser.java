package wci.frontend.pascal.parsers;

import static wci.frontend.pascal.PascalErrorCode.INVALID_INDEX_TYPE;
import static wci.frontend.pascal.PascalErrorCode.MISSING_COMMA;
import static wci.frontend.pascal.PascalErrorCode.MISSING_LEFT_BRACKET;
import static wci.frontend.pascal.PascalErrorCode.MISSING_OF;
import static wci.frontend.pascal.PascalErrorCode.MISSING_RIGHT_BRACKET;
import static wci.frontend.pascal.PascalTokenType.COMMA;
import static wci.frontend.pascal.PascalTokenType.LEFT_BRACKET;
import static wci.frontend.pascal.PascalTokenType.OF;
import static wci.frontend.pascal.PascalTokenType.RIGHT_BRACKET;
import static wci.frontend.pascal.PascalTokenType.SEMICOLON;
import static wci.intermediate.typeimpl.TypeFormImpl.ARRAY;
import static wci.intermediate.typeimpl.TypeFormImpl.ENUMERATION;
import static wci.intermediate.typeimpl.TypeFormImpl.SUBRANGE;
import static wci.intermediate.typeimpl.TypeKeyImpl.ARRAY_ELEMENT_COUNT;
import static wci.intermediate.typeimpl.TypeKeyImpl.ARRAY_ELEMENT_TYPE;
import static wci.intermediate.typeimpl.TypeKeyImpl.ARRAY_INDEX_TYPE;
import static wci.intermediate.typeimpl.TypeKeyImpl.ENUMERATION_CONSTANTS;
import static wci.intermediate.typeimpl.TypeKeyImpl.SUBRANGE_MAX_VALUE;
import static wci.intermediate.typeimpl.TypeKeyImpl.SUBRANGE_MIN_VALUE;

import java.util.ArrayList;
import java.util.EnumSet;

import wci.frontend.Scanner;
import wci.frontend.Token;
import wci.frontend.TokenType;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.SymTabEntry;
import wci.intermediate.TypeFactory;
import wci.intermediate.TypeForm;
import wci.intermediate.TypeSpec;

public class ArrayTypeParser extends TypeSpecificationParser {
	
	private static final EnumSet<PascalTokenType> LEFT_BRACKET_SET =
			SimpleTypeParser.SIMPLE_TYPE_START_SET.clone();
	static {
		LEFT_BRACKET_SET.add(LEFT_BRACKET);
		LEFT_BRACKET_SET.add(RIGHT_BRACKET);
	}
	private static final EnumSet<PascalTokenType> RIGHT_BRACKET_SET =
			EnumSet.of(RIGHT_BRACKET, OF, SEMICOLON);
	private static final EnumSet<PascalTokenType> OF_SET =
			TypeSpecificationParser.TYPE_START_SET.clone();
	static {
		OF_SET.add(OF);
		OF_SET.add(SEMICOLON);
	}
	private static final EnumSet<PascalTokenType> INDEX_START_SET =
			SimpleTypeParser.SIMPLE_TYPE_START_SET.clone();
	static {
		INDEX_START_SET.add(COMMA);
	}
	private static final EnumSet<PascalTokenType> INDEX_END_SET =
			EnumSet.of(RIGHT_BRACKET, OF, SEMICOLON);
	private static final EnumSet<PascalTokenType> INDEX_FOLLOW_SET =
			INDEX_START_SET.clone();
	static {
		INDEX_FOLLOW_SET.addAll(INDEX_END_SET);
	}	
	
	public TypeSpec parse(Token token) 
		throws Exception {
		
		TypeSpec arrayType = TypeFactory.createType(ARRAY);
		token = nextToken();
		
		token = synchronize(LEFT_BRACKET_SET);
		if (token.getType() != LEFT_BRACKET) {
			errorHandler.flag(token, MISSING_LEFT_BRACKET, this);
		}
		
		TypeSpec elementType = parseIndexTypeList(token, arrayType);
		
		token = synchronize(RIGHT_BRACKET_SET);
		if (token.getType() == RIGHT_BRACKET) {
			token = nextToken();
		}
		
		else {
			errorHandler.flag(token, MISSING_RIGHT_BRACKET, this);
		}
		
		token = synchronize(OF_SET);
		if (token.getType() == OF) {
			token = nextToken();
		}
		
		else {
			errorHandler.flag(token, MISSING_OF, this);
		}
		
		elementType.setAttribute(ARRAY_ELEMENT_TYPE, parseElementType(token));
		
		return arrayType;
	}
	
	private TypeSpec parseIndexTypeList(Token token, TypeSpec arrayType) 
		throws Exception 
	{
		TypeSpec elementType = arrayType;
		boolean anotherIndex = false;
		
		token = nextToken();
		
		do {
			anotherIndex = false;
			
			token = synchronize(INDEX_START_SET);
			parseIndexType(token, elementType);
			
			token = synchronize(INDEX_FOLLOW_SET);
			TokenType tokenType = token.getType();
			if ((tokenType != COMMA) && (tokenType != RIGHT_BRACKET)) {
				if (INDEX_START_SET.contains(tokenType)) {
					errorHandler.flag(token, MISSING_COMMA, this);
					anotherIndex = true;
				}
			}
			
			else if (tokenType == COMMA) {
				TypeSpec newElementType = TypeFactory.createType(ARRAY);
				elementType.setAttribute(ARRAY_ELEMENT_TYPE, newElementType);
				elementType = newElementType;
				
				token = nextToken();
				anotherIndex = true;
			}
		} while (anotherIndex);
		
		return elementType;
	}
	
	private void parseIndexType(Token token, TypeSpec arrayType) 
		throws Exception {
		
		SimpleTypeParser simpleTypeParser = new SimpleTypeParser(this);
		TypeSpec indexType = simpleTypeParser.parse(token);
		arrayType.setAttribute(ARRAY_INDEX_TYPE, indexType);
		
		if (indexType == null) {
			return;
		}
		
		TypeForm form = indexType.getForm();
		int count = 0;
		
		if (form == SUBRANGE) {
			Integer minValue = (Integer) indexType.getAttribute(SUBRANGE_MIN_VALUE);
			Integer maxValue = (Integer) indexType.getAttribute(SUBRANGE_MAX_VALUE);
			
			if ((minValue != null) && (maxValue != null)) {
				count = maxValue - minValue + 1;
			}
		}
		
		else if (form == ENUMERATION) {
			ArrayList<SymTabEntry> constants = (ArrayList<SymTabEntry>) 
					indexType.getAttribute(ENUMERATION_CONSTANTS);
			count = constants.size();
		}
		
		else {
			errorHandler.flag(token, INVALID_INDEX_TYPE, this);
		}
		
		arrayType.setAttribute(ARRAY_ELEMENT_COUNT, count);
	}

	private TypeSpec parseElementType(Token token)
		throws Exception{
		
		TypeSpecificationParser typeSpecificationParser = new TypeSpecificationParser(this);
		return typeSpecificationParser.parse(token);
	}
	
	public ArrayTypeParser(Scanner scanner) {
		super(scanner);
	}

	public ArrayTypeParser(PascalParserTD parent) {
		super(parent);
	}

}
