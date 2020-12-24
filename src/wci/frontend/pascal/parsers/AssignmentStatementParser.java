package wci.frontend.pascal.parsers;

import static wci.frontend.pascal.PascalErrorCode.INCOMPATIBLE_TYPES;
import static wci.frontend.pascal.PascalErrorCode.MISSING_COLON_EQUALS;
import static wci.frontend.pascal.PascalTokenType.COLON_EQUALS;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.ASSIGN;

import java.util.EnumSet;

import wci.frontend.Token;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.ICodeFactory;
import wci.intermediate.ICodeNode;
import wci.intermediate.TypeSpec;
import wci.intermediate.symtabimpl.Predefined;
import wci.intermediate.typeimpl.TypeChecker;


public class AssignmentStatementParser extends StatementParser 
{
	private boolean isFunctionTarget = false;
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
		
		ICodeNode assignNode = ICodeFactory.createICodeNode(ASSIGN);
		VariableParser  variableParser = new VariableParser(this);
		ICodeNode targetNode = isFunctionTarget ? variableParser.parseFunctionNameTarget(token) : variableParser.parse(token);
		TypeSpec variableType = targetNode != null ? targetNode.getTypeSpec()
												  : Predefined.undefinedType;
		
		assignNode.addChild(targetNode);
		
		token = synchronize(COLON_EQUALS_SET);
		if (token.getType() == COLON_EQUALS)
		{
			token = nextToken();	
		}
		else
		{
			errorHandler.flag(token, MISSING_COLON_EQUALS, this);
		}
		
		ExpressionParser expressionParser = new ExpressionParser(this);
		ICodeNode exprNode = expressionParser.parse(token);
		assignNode.addChild(exprNode);
		
		TypeSpec exprType = exprNode != null ? exprNode.getTypeSpec()
											: Predefined.undefinedType;
		
		if (!TypeChecker.areAssignmentCompatible(variableType, exprType)) {
			errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
		}
		
		assignNode.setTypeSpec(variableType);
		return assignNode;
	}

	public ICodeNode parseFunctionNameAssignment(Token token) throws Exception {
		isFunctionTarget = true;
		return parse(token);
	}

}
