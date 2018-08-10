package wci.frontend.pascal.parsers;

import static wci.frontend.pascal.PascalErrorCode.*;
import static wci.frontend.pascal.PascalTokenType.*;
import static wci.intermediate.symtabimpl.RoutineCodeImpl.*;
import static wci.intermediate.symtabimpl.DefinitionImpl.*;
import static wci.intermediate.symtabimpl.SymTabKeyImpl.*;

import java.util.ArrayList;
import java.util.EnumSet;

import wci.frontend.Scanner;
import wci.frontend.Token;
import wci.frontend.TokenType;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.Definition;
import wci.intermediate.ICode;
import wci.intermediate.ICodeFactory;
import wci.intermediate.ICodeNode;
import wci.intermediate.SymTab;
import wci.intermediate.SymTabEntry;
import wci.intermediate.TypeForm;
import wci.intermediate.TypeSpec;
import wci.intermediate.symtabimpl.DefinitionImpl;
import wci.intermediate.symtabimpl.Predefined;
import wci.intermediate.typeimpl.TypeFormImpl;

public class DeclaredRoutineParser extends DeclarationsParser {
	
	private static int dummyCounter = 0; //counter for dummy routine names
	private SymTabEntry parentId;
	
	public SymTabEntry parse(Token token, SymTabEntry parentId) 
		throws Exception {
		
		Definition routineDefn = null;
		String dummyName = null;
		SymTabEntry routineId = null;
		TokenType routineType = token.getType();
		
		switch ((PascalTokenType) routineType) {
		
			case PROGRAM: {
				token = nextToken();
				routineDefn = DefinitionImpl.PROGRAM;
				dummyName = "DummyProgramName".toLowerCase();
				break;
			}
			case PROCEDURE: {
				token = nextToken();
				routineDefn = DefinitionImpl.PROCEDURE;
				dummyName = "DummyProcedureName".toLowerCase() + String.format("%03d", ++dummyCounter);
				break;
			}
			case FUNCTION: {
				token = nextToken();
				routineDefn = DefinitionImpl.FUNCTION;
				dummyName = "DummyFunctionName".toLowerCase() + String.format("%03d", ++dummyCounter);
				break;
			}
			
			default: {
				routineDefn = DefinitionImpl.PROGRAM;
				dummyName = "DummyProgramName".toLowerCase();
				break;
			}
		
		}
		
		routineId = parseRoutineName(token, dummyName);
		routineId.setDefinition(routineDefn);
		
		token = currentToken();
		
		ICode iCode = ICodeFactory.createICode();
		routineId.setAttribute(ROUTINE_ICODE, iCode);
		routineId.setAttribute(ROUTINE_ROUTINES, new ArrayList<SymTabEntry>());
		
		if (routineId.getAttribute(ROUTINE_CODE) == FORWARD) {
			SymTab symTab = (SymTab) routineId.getAttribute(ROUTINE_SYMTAB);
			symTabStack.push(symTab);
		}
		else {
			routineId.setAttribute(ROUTINE_SYMTAB, symTabStack.push());
		}
		
		if (routineDefn == DefinitionImpl.PROGRAM) {
			symTabStack.setProgramId(routineId);
		}
		
		else if (routineId.getAttribute(ROUTINE_CODE) != FORWARD) {
			ArrayList<SymTabEntry> subroutines;
			subroutines = (ArrayList<SymTabEntry>) parentId.getAttribute(ROUTINE_ROUTINES);
			subroutines.add(routineId);
		}
		
		if (routineId.getAttribute(ROUTINE_CODE) == FORWARD) {
			if (token.getType() != SEMICOLON) {
				errorHandler.flag(token, ALREADY_FORWARDED, this);
				parseHeader(token, routineId);
			}
		}
		
		else {
			parseHeader(token, routineId);
		}
		
		token = currentToken();
		if (token.getType() == SEMICOLON) {
			do {
				token = nextToken();
			} while (token.getType() == SEMICOLON);
		}
		
		else {
			errorHandler.flag(token, MISSING_SEMICOLON, this);
		}
		
		if ((token.getType() == IDENTIFIER) && (token.getText().equalsIgnoreCase("forward"))) {
			token = nextToken();
			routineId.setAttribute(ROUTINE_CODE, FORWARD);
		}
		else {
			routineId.setAttribute(ROUTINE_CODE, DECLARED);
				
			BlockParser blockParser = new BlockParser(this);
			ICodeNode rootNode = blockParser.parse(token, routineId);
			iCode.setRoot(rootNode);	
		}
		
		symTabStack.pop();		
		return routineId;
	}
	
	private SymTabEntry parseRoutineName(Token token, String dummyName) 
	throws Exception{
		SymTabEntry routineId = null;
		
		if (token.getType() == IDENTIFIER) {
			String routineName = token.getText().toLowerCase();
			routineId = symTabStack.lookupLocal(routineName);
			
			if (routineId == null) {
				routineId = symTabStack.enterLocal(routineName);
			}
					
			else if (routineId.getAttribute(ROUTINE_CODE) != FORWARD) {
				routineId = null;
				errorHandler.flag(token, IDENTIFIER_REDEFINED, this);
			}
			
			token = nextToken();
		}
		else {
			errorHandler.flag(token, MISSING_IDENTIFIER, this);
		}
		
		if (routineId == null) {
			routineId = symTabStack.enterLocal(dummyName);
		}
		
		return routineId;
	}
	
	private void parseHeader(Token token, SymTabEntry routineId) 
		throws Exception{
		
		parseFormalParameters(token, routineId);
		token = currentToken();
		
		if (routineId.getDefinition() == DefinitionImpl.FUNCTION) {
			VariableDeclarationsParser variableDeclarationsParser = new VariableDeclarationsParser(this);
			variableDeclarationsParser.setDefinition(DefinitionImpl.FUNCTION);
			TypeSpec functionReturnType = variableDeclarationsParser.parseTypeSpec(token);
			
			token = currentToken();

			if(functionReturnType != null) {
				TypeForm form = functionReturnType.getForm();
				
				if ((form == TypeFormImpl.ARRAY) || (form == TypeFormImpl.RECORD)) {
					errorHandler.flag(token, INVALID_TYPE, this);
				}
			}
				
			else {
				functionReturnType = Predefined.undefinedType;
			}
			
			routineId.setTypeSpec(functionReturnType);
			token = currentToken();
		}
		
	}
	
	private static final EnumSet<PascalTokenType> PARAMETER_SET =
			DeclarationsParser.DECLARATION_START_SET.clone();
	static {
		PARAMETER_SET.add(VAR);
		PARAMETER_SET.add(IDENTIFIER);
		PARAMETER_SET.add(RIGHT_PAREN);
	}
	
	private static EnumSet<PascalTokenType> LEFT_PAREN_SET = 
		DeclarationsParser.DECLARATION_START_SET.clone();
	static {
		LEFT_PAREN_SET.add(LEFT_PAREN);
		LEFT_PAREN_SET.add(SEMICOLON);
		LEFT_PAREN_SET.add(COLON);
	}
	
	private static final EnumSet<PascalTokenType> RIGHT_PAREN_SET = 
			LEFT_PAREN_SET.clone();
	static {
		RIGHT_PAREN_SET.remove(LEFT_PAREN);
		RIGHT_PAREN_SET.add(RIGHT_PAREN);
	}
	
	protected void parseFormalParameters(Token token, SymTabEntry routineId) 
			throws Exception {
		
		token = synchronize(LEFT_PAREN_SET);
		if (token.getType() == LEFT_PAREN) {
			token = nextToken();
			
			ArrayList<SymTabEntry> params;
			params = new ArrayList<SymTabEntry>();

			token = synchronize(PARAMETER_SET);
			TokenType tokenType = token.getType();
			
			while ((tokenType == IDENTIFIER) || (tokenType == VAR)) {
				params.addAll(parseParamSublist(token, routineId));
				token = currentToken();
				tokenType = token.getType();
			}
			
			if (token.getType() == RIGHT_PAREN) {
				token = nextToken();
			}
			else {
				errorHandler.flag(token, MISSING_RIGHT_PAREN, this);
			}
			
			routineId.setAttribute(ROUTINE_PARMS, params);
			
		}
	}
	
	private static final EnumSet<PascalTokenType> PARAMETER_FOLLOW_SET 
		= EnumSet.of(COLON, RIGHT_PAREN, SEMICOLON);
	static {
		PARAMETER_FOLLOW_SET.addAll(DeclarationsParser.DECLARATION_START_SET);
	}
	private static final EnumSet<PascalTokenType> COMMA_SET 
		= EnumSet.of(COMMA, COLON, IDENTIFIER, RIGHT_PAREN, SEMICOLON);
	static {
		COMMA_SET.addAll(DeclarationsParser.DECLARATION_START_SET);
	}
	
	private ArrayList<SymTabEntry> parseParamSublist(Token token, SymTabEntry routineId) 
		throws Exception {
		
		boolean isProgram = routineId.getDefinition() == DefinitionImpl.PROGRAM;
		Definition parmDefn = isProgram ? PROGRAM_PARM : VAR_PARM;
		TokenType tokenType = token.getType();
		
		if (tokenType == VAR) {
			if (isProgram) {
				errorHandler.flag(token, INVALID_VAR_PARM, this);
			}
			
			token = nextToken();
		}
		else if(!isProgram) parmDefn = VALUE_PARM;
		
		VariableDeclarationsParser variableDeclarationsParser = new VariableDeclarationsParser(this);
		variableDeclarationsParser.setDefinition(parmDefn);
		ArrayList<SymTabEntry> sublist 
			= variableDeclarationsParser.parseIdentifierSublist(token, PARAMETER_FOLLOW_SET, COMMA_SET);
		token = currentToken();
		tokenType = token.getType();
		
		if (!isProgram) {
			if (tokenType == SEMICOLON) {
				while (token.getType() == SEMICOLON) {
					token = nextToken();
				}
			}
			else if (VariableDeclarationsParser.NEXT_START_SET.contains(tokenType)) {
				errorHandler.flag(token, MISSING_SEMICOLON, this);
			}
			token = synchronize(PARAMETER_SET);
		}
		return sublist;
	}

	public DeclaredRoutineParser(Scanner scanner) {
		super(scanner);
	}

	public DeclaredRoutineParser(PascalParserTD parent) {
		super(parent);
	}

}
