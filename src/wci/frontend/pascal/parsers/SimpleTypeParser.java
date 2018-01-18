package wci.frontend.pascal.parsers;

import static wci.frontend.pascal.PascalErrorCode.IDENTIFIER_UNDEFINED;
import static wci.frontend.pascal.PascalErrorCode.INVALID_TYPE;
import static wci.frontend.pascal.PascalErrorCode.NOT_TYPE_IDENTIFIER;
import static wci.frontend.pascal.PascalTokenType.COMMA;
import static wci.frontend.pascal.PascalTokenType.LEFT_PAREN;
import static wci.frontend.pascal.PascalTokenType.SEMICOLON;
import static wci.intermediate.symtabimpl.DefinitionImpl.CONSTANT;
import static wci.intermediate.symtabimpl.DefinitionImpl.ENUMERATION_CONSTANT;


import java.util.EnumSet;

import wci.frontend.Scanner;
import wci.frontend.Token;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.Definition;
import wci.intermediate.SymTabEntry;
import wci.intermediate.TypeSpec;
import wci.intermediate.symtabimpl.DefinitionImpl;

public class SimpleTypeParser extends TypeSpecificationParser {
	
	static final EnumSet<PascalTokenType> SIMPLE_TYPE_START_SET =
			ConstantDefinitionsParser.CONSTANT_START_SET.clone();
	static {
		SIMPLE_TYPE_START_SET.add(LEFT_PAREN);
		SIMPLE_TYPE_START_SET.add(COMMA);
		SIMPLE_TYPE_START_SET.add(SEMICOLON);
	}
	
	public TypeSpec parse(Token token) 
		throws Exception 
	{
		token = synchronize(SIMPLE_TYPE_START_SET);
		
		switch ((PascalTokenType) token.getType()) {
		
			case IDENTIFIER: {
				String name = token.getText();
				SymTabEntry id = symTabStack.lookup(name);
				
				if (id != null) {
					
					Definition definition = id.getDefinition();
					
					if (definition == DefinitionImpl.TYPE) {
						id.appendLineNumber(token.getLineNumber());
						token = nextToken();
						
						return id.getTypeSpec();
					}
					
					else if ((definition != CONSTANT ) && (definition != ENUMERATION_CONSTANT)) {
						errorHandler.flag(token, NOT_TYPE_IDENTIFIER, this);
						token = nextToken();
						return null;
					}
					
					else {
						SubrangeTypeParser subrangeTypeParser = new SubrangeTypeParser(this);
						return subrangeTypeParser.parse(token);
					}
				}
				
				else {
					errorHandler.flag(token, IDENTIFIER_UNDEFINED, this);
					token = nextToken();
					return null;
				}
				
			}
			
			case LEFT_PAREN: {
				EnumerationTypeParser enumerationTypeParser = new EnumerationTypeParser(this);
				return enumerationTypeParser.parse(token);
			}
			
			case COMMA:
			case SEMICOLON: {
				errorHandler.flag(token, INVALID_TYPE, this);
				return null;
			}
			
			
			default: {
				SubrangeTypeParser subrangeTypeParser = new SubrangeTypeParser(this);
				return subrangeTypeParser.parse(token);
			}
		}
		
	}

	public SimpleTypeParser(Scanner scanner) {
		super(scanner);
	}

	public SimpleTypeParser(PascalParserTD parent) {
		super(parent);
	}

}
