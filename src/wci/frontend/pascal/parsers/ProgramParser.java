package wci.frontend.pascal.parsers;

import java.util.EnumSet;

import wci.frontend.Scanner;
import wci.frontend.Token;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.ICodeNode;
import wci.intermediate.SymTabEntry;

import static wci.frontend.pascal.PascalTokenType.*;
import static wci.frontend.pascal.PascalErrorCode.MISSING_PERIOD;

public class ProgramParser extends DeclarationsParser {
	
	static final EnumSet<PascalTokenType> PROGRAM_START_SET = 
			EnumSet.of(PROGRAM, SEMICOLON);
	static {
		PROGRAM_START_SET.addAll(DeclarationsParser.DECLARATION_START_SET);
	}
	
	public SymTabEntry parse(Token token, SymTabEntry parentId) throws Exception {
		
		token = synchronize(PROGRAM_START_SET);
		
		DeclaredRoutineParser routineParser = new DeclaredRoutineParser(this);
		routineParser.parse(token, parentId);
		
		token = currentToken();
		if (token.getType() != DOT) {
			errorHandler.flag(token, MISSING_PERIOD, this);
		}
		
		return null;
	}
	
	public ProgramParser(Scanner scanner) {
		super(scanner);
	}

	public ProgramParser(PascalParserTD parent) {
		super(parent);
	}

}
