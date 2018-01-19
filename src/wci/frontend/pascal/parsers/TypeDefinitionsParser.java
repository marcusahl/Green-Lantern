package wci.frontend.pascal.parsers;

import static wci.frontend.pascal.PascalErrorCode.IDENTIFIER_REDEFINED;
import static wci.frontend.pascal.PascalErrorCode.MISSING_EQUALS;
import static wci.frontend.pascal.PascalErrorCode.MISSING_SEMICOLON;
import static wci.frontend.pascal.PascalTokenType.EQUALS;
import static wci.frontend.pascal.PascalTokenType.IDENTIFIER;
import static wci.frontend.pascal.PascalTokenType.SEMICOLON;
import static wci.intermediate.symtabimpl.DefinitionImpl.TYPE;

import java.util.EnumSet;

import wci.frontend.Scanner;
import wci.frontend.Token;
import wci.frontend.TokenType;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.SymTabEntry;
import wci.intermediate.TypeSpec;

public class TypeDefinitionsParser extends DeclarationsParser {
	
	private static final EnumSet<PascalTokenType> IDENTIFIER_SET =
			DeclarationsParser.VAR_START_SET.clone();
	static {
		IDENTIFIER_SET.add(IDENTIFIER);
	}
	
	private static final EnumSet<PascalTokenType> EQUALS_SET =
			ConstantDefinitionsParser.CONSTANT_START_SET.clone();
	static {
		EQUALS_SET.add(EQUALS);
		EQUALS_SET.add(SEMICOLON);
	}
	
	private static final EnumSet<PascalTokenType> FOLLOW_SET =
			EnumSet.of(SEMICOLON);
	
	private static final EnumSet<PascalTokenType> NEXT_START_SET =
			DeclarationsParser.VAR_START_SET.clone();
	static {
		NEXT_START_SET.add(SEMICOLON);
		NEXT_START_SET.add(IDENTIFIER);
	}
	
	public void parse(Token token) 
		throws Exception
	{
		token = synchronize(IDENTIFIER_SET);
		
		while (token.getType() == IDENTIFIER) {
			String name = token.getText().toLowerCase();
			SymTabEntry typeId = symTabStack.lookup(name);
			
			if (typeId == null) {
				typeId = symTabStack.enterLocal(name);
				typeId.appendLineNumber(token.getLineNumber());
			}
			
			else {
				errorHandler.flag(token, IDENTIFIER_REDEFINED, this);
				typeId = null;
			}
			
			token = nextToken();
			token = synchronize(EQUALS_SET);
			
			if (token.getType() == EQUALS) {
				token = nextToken();
			}
			
			else {
				errorHandler.flag(token, MISSING_EQUALS, this);
			}
			
			TypeSpecificationParser typeSpecificationsParser = new TypeSpecificationParser(this);
			TypeSpec type = typeSpecificationsParser.parse(token);
			
			if (typeId != null) {
				typeId.setDefinition(TYPE);
			}
			
			if ((type != null) && (typeId != null)) {
				
				if (type.getIdentifier() == null) {
					
					type.setIdentifier(typeId);
				}
				typeId.setTypeSpec(type);
			}
			
			else {
				
				token = synchronize(FOLLOW_SET);
			}
			
			token = currentToken();
			TokenType tokenType = token.getType();
			
			if (tokenType == SEMICOLON) {
				
				while (token.getType() == SEMICOLON) {
					token = nextToken();
				}
			}
			
			else if (NEXT_START_SET.contains(tokenType)) {
				errorHandler.flag(token, MISSING_SEMICOLON, this);
			}
			
			token = synchronize(IDENTIFIER_SET);
			
		}
	}
	
	public TypeDefinitionsParser(Scanner scanner) {
		super(scanner);
	}

	public TypeDefinitionsParser(PascalParserTD parent) {
		super(parent);
	}

}
