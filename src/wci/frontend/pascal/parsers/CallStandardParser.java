package wci.frontend.pascal.parsers;

import static wci.frontend.pascal.PascalErrorCode.*;
import static wci.intermediate.icodeimpl.ICodeKeyImpl.ID;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.CALL;
import static wci.intermediate.symtabimpl.SymTabKeyImpl.ROUTINE_CODE;
import static wci.intermediate.symtabimpl.RoutineCodeImpl.*;
import static wci.intermediate.typeimpl.TypeFormImpl.ENUMERATION;


import wci.frontend.Token;
import wci.frontend.pascal.PascalParserTD;
import wci.intermediate.*;
import wci.intermediate.symtabimpl.Predefined;
import wci.intermediate.symtabimpl.RoutineCodeImpl;

public class CallStandardParser extends CallParser {
	
	public ICodeNode parse(Token token) throws Exception {
		ICodeNode callNode = ICodeFactory.createICodeNode(CALL);
		SymTabEntry pfId = symTabStack.lookup(token.getText().toLowerCase());
		RoutineCode routineCode = (RoutineCode) pfId.getAttribute(ROUTINE_CODE);
		callNode.setAttribute(ID, pfId);
		
		token = nextToken();
		
		switch ((RoutineCodeImpl) routineCode) {
			
			case READ:
			case READLN: return parseReadReadln(token, callNode, pfId);
			
			case WRITE:
			case WRITELN: return parseWriteWriteln(token, callNode, pfId);
			
			case EOF:
			case EOLN: return parseEofEoln(token, callNode, pfId);
			
			case ABS:
			case SQR: return parseAbsSqr(token, callNode, pfId);
			
			case ARCTAN:
			case COS:
			case EXP:
			case LN:
			case SIN:
			case SQRT: return parseArctanCosExpLnSinSqrt(token, callNode, pfId);
			
			case PRED:
			case SUCC: return parsePredSuc(token, callNode, pfId);
			
			case CHR: return parseChr(token, callNode, pfId);
			case ODD: return parseOdd(token, callNode, pfId);
			case ORD: return parseOrd(token, callNode, pfId);
			
			case ROUND:
			case TRUNC: return parseRoundTrunc(token, callNode, pfId);
			
			default: return null;
		
		}
		
	}

	private ICodeNode parseReadReadln(Token token, ICodeNode callNode, SymTabEntry routineId) throws Exception {
		ICodeNode paramsNode = parseActualParameters(token, routineId, false, true, false);
		
		callNode.addChild(paramsNode);
		
		if (routineId == Predefined.readId && callNode.getChildren().size() == 0) {
			errorHandler.flag(token, WRONG_NUMBER_OF_PARMS, this);
		}
		
		return callNode;
	}

	private ICodeNode parseWriteWriteln(Token token, ICodeNode callNode, SymTabEntry routineId) throws Exception {
		ICodeNode paramsNode = parseActualParameters(token, routineId, false, false, true);
		callNode.addChild(paramsNode);

		if (routineId == Predefined.writeId && callNode.getChildren().size() == 0) {
			errorHandler.flag(token, WRONG_NUMBER_OF_PARMS, this);
		}
		return callNode;
	}

	private ICodeNode parseEofEoln(Token token, ICodeNode callNode, SymTabEntry routineId) throws Exception {
		ICodeNode paramsNode = parseStandardParameters(token, routineId);
		callNode.addChild(paramsNode);

		if (checkParamsCount(token, paramsNode, 0)) {
			callNode.setTypeSpec(Predefined.booleanType);
		}
		return callNode;
	}

	private ICodeNode parseAbsSqr(Token token, ICodeNode callNode, SymTabEntry routineId) throws Exception {
		ICodeNode paramsNode = parseStandardParameters(token, routineId);
		callNode.addChild(paramsNode);

		if (checkParamsCount(token, paramsNode, 1)) {
			TypeSpec argType = paramsNode.getChildren().get(0).getTypeSpec().baseType();
			if (argType == Predefined.integerType || argType == Predefined.realType) {
				callNode.setTypeSpec(argType);
			} else {
				errorHandler.flag(token, INVALID_TYPE, this);
			}
		}
		return callNode;
	}

	private ICodeNode parseArctanCosExpLnSinSqrt(Token token, ICodeNode callNode, SymTabEntry routineId) throws Exception {
		ICodeNode paramsNode = parseStandardParameters(token, routineId);
		callNode.addChild(paramsNode);

		if (checkParamsCount(token, paramsNode, 1)) {
			TypeSpec argType = paramsNode.getChildren().get(0).getTypeSpec().baseType();
			if (argType == Predefined.integerType || argType == Predefined.realType) {
				callNode.setTypeSpec(argType);
			} else {
				errorHandler.flag(token, INVALID_TYPE, this);
			}
		}
		return callNode;
	}

	private ICodeNode parsePredSuc(Token token, ICodeNode callNode, SymTabEntry routineId) throws Exception {
		ICodeNode paramsNode = parseStandardParameters(token, routineId);
		callNode.addChild(paramsNode);

		if (checkParamsCount(token, paramsNode, 1)) {
			TypeSpec argType = paramsNode.getChildren().get(0).getTypeSpec().baseType();
			if (argType == Predefined.integerType || argType.getForm() == ENUMERATION) {
				callNode.setTypeSpec(argType);
			} else {
				errorHandler.flag(token, INVALID_TYPE, this);
			}
		}
		return callNode;
	}

	private ICodeNode parseChr(Token token, ICodeNode callNode, SymTabEntry routineId) throws Exception {
		ICodeNode paramsNode = parseStandardParameters(token, routineId);
		callNode.addChild(paramsNode);

		if (checkParamsCount(token, paramsNode, 1)) {
			TypeSpec argType = paramsNode.getChildren().get(0).getTypeSpec().baseType();
			if (argType == Predefined.integerType) {
				callNode.setTypeSpec(Predefined.charType);
			} else {
				errorHandler.flag(token, INVALID_TYPE, this);
			}
		}
		return callNode;
	}

	private ICodeNode parseOdd(Token token, ICodeNode callNode, SymTabEntry routineId) throws Exception {
		ICodeNode paramsNode = parseStandardParameters(token, routineId);
		callNode.addChild(paramsNode);

		if (checkParamsCount(token, paramsNode, 1)) {
			TypeSpec argType = paramsNode.getChildren().get(0).getTypeSpec().baseType();
			if (argType == Predefined.integerType) {
				callNode.setTypeSpec(Predefined.booleanType);
			} else {
				errorHandler.flag(token, INVALID_TYPE, this);
			}
		}
		return callNode;
	}

	private ICodeNode parseOrd(Token token, ICodeNode callNode, SymTabEntry routineId) throws Exception {
		ICodeNode paramsNode = parseStandardParameters(token, routineId);
		callNode.addChild(paramsNode);

		if (checkParamsCount(token, paramsNode, 1)) {
			TypeSpec argType = paramsNode.getChildren().get(0).getTypeSpec().baseType();
			if (argType == Predefined.charType || argType.getForm() == ENUMERATION) {
				callNode.setTypeSpec(Predefined.integerType);
			} else {
				errorHandler.flag(token, INVALID_TYPE, this);
			}
		}
		return callNode;
	}

	private ICodeNode parseRoundTrunc(Token token, ICodeNode callNode, SymTabEntry routineId) throws Exception {
		ICodeNode paramsNode = parseStandardParameters(token, routineId);
		callNode.addChild(paramsNode);

		if (checkParamsCount(token, paramsNode, 1)) {
			TypeSpec argType = paramsNode.getChildren().get(0).getTypeSpec().baseType();
			if (argType == Predefined.realType) {
				callNode.setTypeSpec(Predefined.integerType);
			} else {
				errorHandler.flag(token, INVALID_TYPE, this);
			}
		}
		return callNode;
	}

	private boolean checkParamsCount(Token token, ICodeNode paramsNode, int count) {
		if ((paramsNode == null && count == 0) || paramsNode.getChildren().size() == count) {
			return true;
		}
		errorHandler.flag(token, WRONG_NUMBER_OF_PARMS, this);
		return false;
	}

	// Wrapper to remove some arguments from most calls
	// Most common case is that parameters are not declared, not ReadLn, and not WriteLn
	private ICodeNode parseStandardParameters(Token token, SymTabEntry routineId) throws Exception {
		return parseActualParameters(token, routineId, false, false, false);
	}

	public CallStandardParser(PascalParserTD parent) {
		super(parent);
	}

}
