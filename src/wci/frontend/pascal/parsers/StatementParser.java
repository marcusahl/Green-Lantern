package wci.frontend.pascal.parsers;

import wci.frontend.*;
import wci.frontend.pascal.*;
import wci.intermediate.Definition;
import wci.intermediate.ICodeFactory;
import wci.intermediate.ICodeNode;
import wci.intermediate.SymTabEntry;
import wci.intermediate.symtabimpl.DefinitionImpl;

import static wci.frontend.pascal.PascalTokenType.*;
import static wci.frontend.pascal.PascalErrorCode.*;
import static wci.intermediate.icodeimpl.ICodeKeyImpl.*;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.*;
import static wci.intermediate.symtabimpl.DefinitionImpl.UNDEFINED;

import java.util.EnumSet;
import java.util.Locale;

public class StatementParser extends PascalParserTD 
{

	protected static final EnumSet<PascalTokenType> STMT_START_SET =
			EnumSet.of(BEGIN, CASE, FOR, PascalTokenType.IF, REPEAT, WHILE, 
					IDENTIFIER, SEMICOLON);
	
	protected static final EnumSet<PascalTokenType> STMT_FOLLOW_SET =
			EnumSet.of(SEMICOLON, END, ELSE, UNTIL, DOT);
	
	public StatementParser(PascalParserTD parent) 
	{
		super(parent);
	}
		
	public ICodeNode parse(Token token) 
		throws Exception
	{
		ICodeNode statementNode = null;
		
		
		// TODO lots of code repetition
		// Try to make this cleaner
		
		switch ((PascalTokenType) token.getType())
		{
			case BEGIN:
			{
				CompoundStatementParser compoundParser = new CompoundStatementParser(this);
				statementNode = compoundParser.parse(token);
				break;
			}
			
			case IDENTIFIER:
			{
				String name = token.getText().toLowerCase();
				SymTabEntry id = symTabStack.lookup(name);
				Definition idDef = id != null ? id.getDefinition() : UNDEFINED;

				switch ((DefinitionImpl) idDef) {
					case VARIABLE:
					case VALUE_PARM:
					case VAR_PARM:
					case UNDEFINED: {
						AssignmentStatementParser assignmentParser = new AssignmentStatementParser(this);
						statementNode = assignmentParser.parse(token);
						break;
					}
					case FUNCTION: {
						AssignmentStatementParser assignmentParser = new AssignmentStatementParser(this);
						statementNode = assignmentParser.parseFunctionNameAssignment(token);
						break;
					}
					case PROCEDURE: {
						CallParser callParser = new CallParser(this);
						statementNode = callParser.parse(token);
						break;
					}
					default: {
						errorHandler.flag(token, UNEXPECTED_TOKEN, this);
						token = nextToken();
					}
				}

				break;
			}
			
			case REPEAT:
			{
				RepeatStatementParser repeatParser = new RepeatStatementParser(this);
				statementNode = repeatParser.parse(token);
				break;
			}
			
			case WHILE:
			{
				WhileStatementParser whileParser = new WhileStatementParser(this);
				statementNode = whileParser.parse(token);
				break;
			}
			
			case FOR:
			{
				ForStatementParser forParser = new ForStatementParser(this);
				statementNode = forParser.parse(token);
				break;
			}
			
			case IF: 
			{
				IfStatementParser ifParser = new IfStatementParser(this);
				statementNode = ifParser.parse(token);
				break;
			}
			
			case CASE:
			{
				CaseStatementParser caseParser = new CaseStatementParser(this);
				statementNode = caseParser.parse(token);
				break;
			} 
			
			default:
			{
				statementNode = ICodeFactory.createICodeNode(NO_OP);
				break;
			}
		}
		
		// Set the current line number as an attribute
		setLineNumber(statementNode, token);
		
		return statementNode;
	}
	
	protected void setLineNumber(ICodeNode node, Token token)
	{
		if (node != null)
		{
			node.setAttribute(LINE, token.getLineNumber());
		}
	}
	
	protected void parseList(Token token, ICodeNode parentNode, PascalTokenType terminator,
							PascalErrorCode errorCode) throws Exception {
		
		EnumSet<PascalTokenType> terminators = STMT_START_SET.clone();
		terminators.add(terminator);
		

		while (!(token instanceof EofToken) && (token.getType() != terminator))
		{
			ICodeNode statementNode = parse(token);
			parentNode.addChild(statementNode);
			token = currentToken();
			TokenType tokenType = token.getType();
			
			if (tokenType == SEMICOLON)
			{
				token = nextToken();
			} else if (STMT_START_SET.contains(token.getType())) {
				errorHandler.flag(token, MISSING_SEMICOLON, this);
			}
			token = synchronize(terminators);
		}

		if (token.getType() == terminator)
		{
			token = nextToken();
		} else {
			errorHandler.flag(token, errorCode, this);
		}
		
	}

}
