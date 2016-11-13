package wci.frontend.pascal.parsers;

import wci.frontend.pascal.*;
import wci.frontend.*;
import wci.intermediate.*;

import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.*;
import static wci.frontend.pascal.PascalTokenType.*;
import static wci.frontend.pascal.PascalErrorCode.*;
import static wci.intermediate.icodeimpl.ICodeKeyImpl.*;


public class AssignmentStatementParser extends StatementParser 
{

	public AssignmentStatementParser(PascalParserTD parent) 
	{
		super(parent);
	}
	
	
	/**
	 * Parses the assignment statement
	 * @param token the assignment token.
	 * @return the root node of the generated parse tree.
	 * @throws Exception if an error occurred.
	 */
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
		
		token = nextToken();					//	consume the identifier class
		
		//	Create the variable node and get its name attribute.
		ICodeNode variableNode = ICodeFactory.createICodeNode(VARIABLE);
		variableNode.setAttribute(ID, targetId);
		
		//	The ASSIGN node adopts the variable node as its first child.
		assignNode.addChild(variableNode);
		
		// Look for the := token
		if (token.getType() == COLON_EQUALS)
		{
			token = nextToken();					// consume the :=
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
