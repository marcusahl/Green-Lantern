package wci.frontend.pascal.parsers;

import wci.frontend.*;
import wci.frontend.pascal.*;
import wci.intermediate.ICodeFactory;
import wci.intermediate.ICodeNode;

import static wci.frontend.pascal.PascalTokenType.*;
import static wci.frontend.pascal.PascalErrorCode.*;
import static wci.intermediate.icodeimpl.ICodeKeyImpl.*;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.*;

import java.util.EnumSet;

public class StatementParser extends PascalParserTD 
{

	// Synchronization set for starting a statement.
	protected static final EnumSet<PascalTokenType> STMT_START_SET = 
			EnumSet.of(BEGIN, CASE, FOR, PascalTokenType.IF, REPEAT, WHILE, 
					IDENTIFIER, SEMICOLON);
	
	// Synchronization set for following statement.
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
				AssignmentStatementParser assignmentParser = new AssignmentStatementParser(this);
				statementNode = assignmentParser.parse(token);
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
		
		// Loops to parse each statement until the END token
		// or the end of the source file.
		while (!(token instanceof EofToken) && (token.getType() != terminator))
		{
			//	Parse a statement. The parent node adopts the statement node.
			ICodeNode statementNode = parse(token);
			parentNode.addChild(statementNode);
			
			token = currentToken();
			TokenType tokenType = token.getType();
			
			// Look for the semicolon between statements.
			if (tokenType == SEMICOLON)
			{
				// consume the ;
				token = nextToken();
			}
			
			// If at the start of the next assignment statement
			// then missing a semicolon
			else if (STMT_START_SET.contains(token.getType())) {
				errorHandler.flag(token, MISSING_SEMICOLON, this);
			}
			
			// Synchronize at the start of the next statement
			// or at the terminator.
			token = synchronize(terminators);
			
		}
			
		// Look for the terminator token
		if (token.getType() == terminator)
		{
			// consume the terminator token
			token = nextToken();
		}
		else
		{
			errorHandler.flag(token, errorCode, this);
		}
		
	}

}
