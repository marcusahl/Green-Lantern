package wci.frontend.pascal.parsers;

import static wci.frontend.pascal.PascalErrorCode.INCOMPATIBLE_TYPES;
import static wci.frontend.pascal.PascalErrorCode.MISSING_DO;
import static wci.frontend.pascal.PascalErrorCode.MISSING_TO_DOWNTO;
import static wci.frontend.pascal.PascalTokenType.DO;
import static wci.frontend.pascal.PascalTokenType.DOWNTO;
import static wci.frontend.pascal.PascalTokenType.TO;
import static wci.intermediate.icodeimpl.ICodeKeyImpl.VALUE;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.ADD;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.ASSIGN;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.COMPOUND;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.GT;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.INTEGER_CONSTANT;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.LOOP;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.LT;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.SUBTRACT;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.TEST;
import static wci.intermediate.typeimpl.TypeFormImpl.ENUMERATION;

import java.util.EnumSet;

import wci.frontend.Token;
import wci.frontend.TokenType;
import wci.frontend.pascal.PascalParserTD;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.ICodeFactory;
import wci.intermediate.ICodeNode;
import wci.intermediate.TypeSpec;
import wci.intermediate.symtabimpl.Predefined;
import wci.intermediate.typeimpl.TypeChecker;

public class ForStatementParser extends StatementParser {

	static final EnumSet<PascalTokenType> TO_DOWNTO_SET =
			ExpressionParser.EXPR_START_SET.clone();
	static {
		TO_DOWNTO_SET.add(TO);
		TO_DOWNTO_SET.add(DOWNTO);
		TO_DOWNTO_SET.addAll(StatementParser.STMT_FOLLOW_SET);
	}
	private static final EnumSet<PascalTokenType> DO_SET = 
			StatementParser.STMT_START_SET.clone();
	static {
		DO_SET.add(DO);
		DO_SET.addAll(StatementParser.STMT_FOLLOW_SET);
	}
	
	public ForStatementParser(PascalParserTD parent) {
		super(parent);
	}
	
	public ICodeNode parse(Token token) throws Exception {
	
		token = nextToken();
		Token targetToken = token;
		
		ICodeNode compoundNode = ICodeFactory.createICodeNode(COMPOUND);
		ICodeNode loopNode = ICodeFactory.createICodeNode(LOOP);
		ICodeNode testNode = ICodeFactory.createICodeNode(TEST);
		
		AssignmentStatementParser assignmentParser = new AssignmentStatementParser(this);
		ICodeNode initAssignNode = assignmentParser.parse(token);
		TypeSpec controlType = initAssignNode != null ? initAssignNode.getTypeSpec()
													 : Predefined.undefinedType;
		
		if (!TypeChecker.isInteger(controlType) && 
			(controlType.getForm() != ENUMERATION)) {
			
			errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
		}
		
		compoundNode.addChild(initAssignNode);
		compoundNode.addChild(loopNode);
		
		token = synchronize(TO_DOWNTO_SET);
		TokenType direction = token.getType();
		
		if ((direction == TO) || (direction == DOWNTO)) {
			token = nextToken();
		}
		
		else {
			direction = TO;
			errorHandler.flag(token, MISSING_TO_DOWNTO, this);			
		}
		
		ICodeNode relOpNode = ICodeFactory.createICodeNode(direction == TO ? GT : LT);
		relOpNode.setTypeSpec(Predefined.booleanType);
		
		ICodeNode controlVarNode = initAssignNode.getChildren().get(0);
		relOpNode.addChild(controlVarNode.copy());
		
		ExpressionParser expressionParser = new ExpressionParser(this);
		ICodeNode exprNode = expressionParser.parse(token);
		relOpNode.addChild(exprNode);
		
		TypeSpec exprType = exprNode != null ? exprNode.getTypeSpec()
											: Predefined.undefinedType;
		if (!TypeChecker.areAssignmentCompatible(controlType, exprType)) {
			errorHandler.flag(token, INCOMPATIBLE_TYPES, this);
		}
		
		testNode.addChild(relOpNode);
		loopNode.addChild(testNode);
		
		token = synchronize(DO_SET);
		if (token.getType() == DO) {
			token = nextToken();
		}
		
		else {
			errorHandler.flag(token, MISSING_DO, this);
		}
		
		StatementParser statementParser = new StatementParser(this);
		loopNode.addChild(statementParser.parse(token));
		
		ICodeNode nextAssignNode = ICodeFactory.createICodeNode(ASSIGN);
		nextAssignNode.addChild(controlVarNode.copy());
		
		ICodeNode arithOpNode = ICodeFactory.createICodeNode(direction == TO
				? ADD : SUBTRACT);
		
		arithOpNode.addChild(controlVarNode.copy());
		ICodeNode oneNode = ICodeFactory.createICodeNode(INTEGER_CONSTANT);
		oneNode.setAttribute(VALUE, 1);
		arithOpNode.addChild(oneNode);
		
		nextAssignNode.addChild(arithOpNode);
		loopNode.addChild(nextAssignNode);
		
		setLineNumber(nextAssignNode, targetToken);
		
		return compoundNode;
		
	}


}
