package wci.frontend.pascal.parsers;

import static wci.frontend.pascal.PascalErrorCode.MISSING_COLON_EQUALS;
import static wci.frontend.pascal.PascalTokenType.COLON_EQUALS;
import static wci.intermediate.icodeimpl.ICodeKeyImpl.ID;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.ASSIGN;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.VARIABLE;

import java.util.EnumSet;

import wci.frontend.Token;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.ICodeFactory;
import wci.intermediate.ICodeNode;
import wci.intermediate.SymTabEntry;


public class AssignmentStatementParser extends StatementParser 
{
	
	private static final EnumSet<PascalTokenType> COLON_EQUALS_SET = 
			ExpressionParser.EXPR_START_SET.clone();
	
	static {
		COLON_EQUALS_SET.add(COLON_EQUALS);
		COLON_EQUALS_SET.addAll(StatementParser.STMT_FOLLOW_SET);
	}

	public AssignmentStatementParser(PascalParserTD parent) 
	{
		super(parent);
	}
	
	
	public ICodeNode parse(Token token)
		throws Exception
	{
		// create the ASSIGN node
		ICodeNode assignNode = ICodeFactory.createICodeNode(ASSIGN);
		
		//	Look up the target identifier in the symbol table stack.
		// Enter the identifier into the table if it's not found
		String targetName = token.getText().toLowerCase();
		SymTabEntry targetId = symTabStack.lookup(targetName);
		if (targetId == null)
		{
			targetId = symTabStack.enterLocal(targetName);
		}
		targetId.appendLineNumber(token.getLineNumber());
		
		// consume the identifier class
		token = nextToken();	
		
		//	Create the variable node and get its name attribute.
		ICodeNode variableNode = ICodeFactory.createICodeNode(VARIABLE);
		variableNode.setAttribute(ID, targetId);
		
		// The ASSIGN node adopts the variable node as its first child.
		assignNode.addChild(variableNode);
		
		// Synchronize on the := token
		token = synchronize(COLON_EQUALS_SET);
		if (token.getType() == COLON_EQUALS)
		{
			// consume the :=
			token = nextToken();	
		}
		else
		{
			errorHandler.flag(token, MISSING_COLON_EQUALS, this);
		}
		
		//	Parse the expression. The ASSIGN node adopts the expression's
		//	node as its second child.
		ExpressionParser expressionParser = new ExpressionParser(this);
		assignNode.addChild(expressionParser.parse(token));
		
		return assignNode;
	}

}
