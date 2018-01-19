package wci.frontend.pascal.parsers;

import static wci.frontend.pascal.PascalErrorCode.MISSING_END;
import static wci.frontend.pascal.PascalTokenType.END;
import static wci.frontend.pascal.PascalTokenType.SEMICOLON;
import static wci.intermediate.symtabimpl.DefinitionImpl.FIELD;
import static wci.intermediate.typeimpl.TypeFormImpl.RECORD;
import static wci.intermediate.typeimpl.TypeKeyImpl.RECORD_SYMTAB;

import java.util.EnumSet;

import wci.frontend.Scanner;
import wci.frontend.Token;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.TypeFactory;
import wci.intermediate.TypeSpec;

public class RecordTypeParser extends TypeSpecificationParser {

	private static final EnumSet<PascalTokenType> END_SET =
			DeclarationsParser.VAR_START_SET.clone();
	static {
		END_SET.add(END);
		END_SET.add(SEMICOLON);
	}
	
	public TypeSpec parse(Token token) 
		throws Exception {
		
		TypeSpec recordType = TypeFactory.createType(RECORD);
		token = nextToken();
		
		recordType.setAttribute(RECORD_SYMTAB, symTabStack.push());
		
		VariableDeclarationsParser variableDeclarationsParser = new VariableDeclarationsParser(this);
		variableDeclarationsParser.setDefinition(FIELD);
		variableDeclarationsParser.parse(token);
		
		symTabStack.pop();
		
		token = synchronize(END_SET);
		
		if (token.getType() == END) {
			token = nextToken();
		}
		
		else {
			errorHandler.flag(token, MISSING_END, this);
		}
		
		return recordType;
	}
	
	public RecordTypeParser(Scanner scanner) {
		super(scanner);
	}

	public RecordTypeParser(PascalParserTD parent) {
		super(parent);
	}

}
