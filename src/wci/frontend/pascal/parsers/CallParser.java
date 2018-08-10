package wci.frontend.pascal.parsers;

import static wci.frontend.pascal.PascalErrorCode.*;
import static wci.frontend.pascal.PascalTokenType.*;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.*;
import static wci.intermediate.symtabimpl.DefinitionImpl.VAR_PARM;
import static wci.intermediate.symtabimpl.RoutineCodeImpl.DECLARED;
import static wci.intermediate.symtabimpl.RoutineCodeImpl.FORWARD;
import static wci.intermediate.symtabimpl.SymTabKeyImpl.ROUTINE_CODE;
import static wci.intermediate.symtabimpl.SymTabKeyImpl.ROUTINE_PARMS;
import static wci.intermediate.typeimpl.TypeFormImpl.SCALAR;
import static wci.intermediate.typeimpl.TypeFormImpl.SUBRANGE;

import java.beans.Expression;
import java.util.ArrayList;
import java.util.EnumSet;

import wci.frontend.Token;
import wci.frontend.TokenType;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.*;
import wci.intermediate.icodeimpl.ICodeNodeTypeImpl;
import wci.intermediate.symtabimpl.Predefined;
import wci.intermediate.typeimpl.TypeChecker;

public class CallParser extends StatementParser {
	
	public ICodeNode parse(Token token) 
		throws Exception
	{
		SymTabEntry routineId = symTabStack.lookup(token.getText().toLowerCase());
		RoutineCode routineCode = (RoutineCode) routineId.getAttribute(ROUTINE_CODE);
		StatementParser callParser = (routineCode == DECLARED || routineCode == FORWARD)
				? new CallDeclaredParser(this)
				: new CallStandardParser(this);
		
		return callParser.parse(token);
	}
	private static final EnumSet<PascalTokenType> COMMA_SET = EnumSet.of(COMMA, RIGHT_PAREN);
	static {
		COMMA_SET.addAll(ExpressionParser.EXPR_START_SET);
	}
	protected ICodeNode parseActualParameters(
			Token token, 
			SymTabEntry routineId,
			boolean isDeclared,
			boolean isReadReadln,
			boolean isWriteWriteLn
		) throws Exception
	{
		ExpressionParser expressionParser = new ExpressionParser(this);
		ICodeNode paramsNode = ICodeFactory.createICodeNode(PARAMETERS);
		ArrayList<SymTabEntry> formalParms = null;
		int paramCount = 0;
		int paramIndex = -1;
		
		if (isDeclared) {
			formalParms = (ArrayList<SymTabEntry>) routineId.getAttribute(ROUTINE_PARMS);
			paramCount = formalParms != null ? formalParms.size() : 0;
		}
		
		if (token.getType() != LEFT_PAREN) {
			if (paramCount != 0) {
				errorHandler.flag(token, WRONG_NUMBER_OF_PARMS, this);
			}
			return null;
		}
		
		token = nextToken();
		
		while (token.getType() != RIGHT_PAREN) {
			ICodeNode actualNode = expressionParser.parse(token);
			
			if (isDeclared) {
				if (++paramIndex < paramCount) {
					SymTabEntry formalId = formalParms.get(paramIndex);
					checkActualParameter(token, formalId, actualNode);
				}
				else if (paramIndex == paramCount) {
					errorHandler.flag(token, WRONG_NUMBER_OF_PARMS, this);

				}
			}
			else if (isReadReadln) {
				TypeSpec type = actualNode.getTypeSpec();
				TypeForm form = type.getForm();

				// Parameter must be a Variable of type Scalar, Boolean, or a Subrange of Integers
				if ( !(actualNode.getType() == ICodeNodeTypeImpl.VARIABLE) &&
						(
							(form == SCALAR || type == Predefined.booleanType) ||
							(form == SUBRANGE && type.baseType() == Predefined.integerType)
						)
					)
				{
					errorHandler.flag(token, INVALID_VAR_PARM, this);
				}
			}
		
			else if (isWriteWriteLn) {
				
				ICodeNode exprNode = actualNode;
				actualNode = ICodeFactory.createICodeNode(WRITE_PARM);
				actualNode.addChild(exprNode);

				TypeSpec type = exprNode.getTypeSpec().baseType();
				TypeForm form = type.getForm();

				// Parameter must be a Scaler, Boolean, or a Pascal String
				if (! ((form == SCALAR) || (type == Predefined.booleanType) || (type.isPascalString())) )
				{
					errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
				}
				
				// Optional field width
				token = currentToken();
				actualNode.addChild(parseWriteSpec(token));
				
				// Optional precision
				token = currentToken();
				actualNode.addChild(parseWriteSpec(token));
				
			}
			paramsNode.addChild(actualNode);
			token = synchronize(COMMA_SET);
			TokenType tokenType = token.getType();
			if (tokenType == COMMA) {
				token = nextToken();
			} else if (ExpressionParser.EXPR_START_SET.contains(tokenType)) {
				errorHandler.flag(token, MISSING_COMMA, this);
			} else if (tokenType != RIGHT_PAREN) {
				token = synchronize(ExpressionParser.EXPR_START_SET);
			}
		}

		token = nextToken();
		if (paramsNode.getChildren().size() == 0 || (isDeclared && (paramIndex != paramCount - 1))) {
			errorHandler.flag(token, WRONG_NUMBER_OF_PARMS, this);
		}
		return paramsNode;
	}

	private void checkActualParameter(Token token, SymTabEntry formalId, ICodeNode actualNode) {
		Definition formalDef = formalId.getDefinition();
		TypeSpec formalType = formalId.getTypeSpec();
		TypeSpec actualType = actualNode.getTypeSpec();

		if (formalDef == VAR_PARM) {
			if (actualNode.getType() != VARIABLE || actualType != formalType) {
				errorHandler.flag(token, INVALID_VAR_PARM, this);
			}
		} else if (!TypeChecker.areAssignmentCompatible(formalType, actualType)) {
			errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
		}
	}

	private ICodeNode parseWriteSpec(Token token) throws Exception {
		if (token.getType() != COLON) {
			return null;
		}
		token = nextToken();
		ExpressionParser expressionParser = new ExpressionParser(this);
		ICodeNode specNode = expressionParser.parse(token);
		if (specNode.getType() == INTEGER_CONSTANT) {
			return specNode;
		} else {
			errorHandler.flag(token, INVALID_NUMBER, this);
			return null;
		}
	}


	public CallParser(PascalParserTD parent) {
		super(parent);
	}

}
