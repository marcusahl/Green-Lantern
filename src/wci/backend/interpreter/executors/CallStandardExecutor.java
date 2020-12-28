package wci.backend.interpreter.executors;

import wci.backend.interpreter.Cell;
import wci.backend.interpreter.Executor;
import wci.frontend.Source;
import wci.frontend.Token;
import wci.frontend.TokenType;
import wci.intermediate.*;
import wci.intermediate.symtabimpl.Predefined;
import wci.intermediate.symtabimpl.RoutineCodeImpl;

import java.util.ArrayList;

import static wci.backend.interpreter.RuntimeErrorCode.INVALID_INPUT;
import static wci.backend.interpreter.RuntimeErrorCode.INVALID_STANDARD_FUNCTION_ARGUMENT;
import static wci.frontend.pascal.PascalTokenType.*;
import static wci.intermediate.icodeimpl.ICodeKeyImpl.ID;
import static wci.intermediate.icodeimpl.ICodeKeyImpl.VALUE;
import static wci.intermediate.symtabimpl.RoutineCodeImpl.*;
import static wci.intermediate.symtabimpl.SymTabKeyImpl.ROUTINE_CODE;

public class CallStandardExecutor extends CallExecutor {

    private ExpressionExecutor expressionExecutor;

    public CallStandardExecutor(Executor parent) { super(parent); }

    public Object execute(ICodeNode node) {
        SymTabEntry routineId = (SymTabEntry) node.getAttribute(ID);
        RoutineCode routineCode = (RoutineCode) routineId.getAttribute(ROUTINE_CODE);
        TypeSpec type = node.getTypeSpec();
        ICodeNode actualNode = null;
        expressionExecutor = new ExpressionExecutor(this);

        if (node.getChildren().size() > 0) {
            ICodeNode paramsNode = node.getChildren().get(0);
            actualNode = paramsNode.getChildren().get(0);
        }

        switch ((RoutineCodeImpl) routineCode) {
            case READ:
            case READLN: return executeReadReadln(node, routineCode);

            case WRITE:
            case WRITELN: return executeWriteWriteln(node, routineCode);

            case EOF:
            case EOLN: return executeEofEoln(node, routineCode);

            case ABS:
            case SQR: return executeAbsSqr(node, routineCode, actualNode);

            case ARCTAN:
            case COS:
            case EXP:
            case LN:
            case SIN:
            case SQRT: return executeArctanCosExpLnSinSqrt(node, routineCode, actualNode);

            case PRED:
            case SUCC: return executePredSucc(node, routineCode, actualNode, type);

            case CHR: return executeChr(node, routineCode, actualNode);
            case ODD: return executeOdd(node, routineCode, actualNode);
            case ORD: return excuteOrd(node, routineCode, actualNode);

            case ROUND:
            case TRUNC: return executeRoundTrunc(node, routineCode, actualNode);

            default: return null; // FORWARD or DECLARED
        }
    }

    private Object executeReadReadln(ICodeNode callNode, RoutineCode routineCode) {
        ICodeNode paramsNode = callNode.getChildren().size() > 0
                ? callNode.getChildren().get(0)
                : null;
        if (paramsNode != null) {
            ArrayList<ICodeNode> actuals = paramsNode.getChildren();
            for (ICodeNode actualNode : actuals) {
                TypeSpec type = actualNode.getTypeSpec();
                TypeSpec baseType = type.baseType();
                Cell variableCell = expressionExecutor.executeVariable(actualNode);
                Object value;

                try {
                    if (baseType == Predefined.integerType) {
                        Token token = standardIn.nextToken();
                        value = (Integer) parseNumber(token, baseType);
                    } else if (baseType == Predefined.realType){
                        Token token = standardIn.nextToken();
                        value = (Float) parseNumber(token, baseType);
                    } else if (baseType == Predefined.booleanType) {
                        Token token = standardIn.nextToken();
                        value = parseBoolean(token);
                    } else if (baseType == Predefined.charType) {
                        char ch = standardIn.nextChar();
                        if (ch == Source.EOL || ch == Source.EOF) {
                            ch = ' ';
                        }
                        value = ch;
                    } else { throw new Exception(); }
                } catch (Exception e) {
                    errorHandler.flag(callNode, INVALID_INPUT, this);

                    if (type == Predefined.realType) {
                        value = 0.0f;
                    } else if (type == Predefined.charType) {
                        value = ' ';
                    } else if (type == Predefined.booleanType) {
                        value = false;
                    } else {
                        value = 0;
                    }
                }
                value = checkRange(callNode, type, value);
                variableCell.setValue(value);
                SymTabEntry actualId = (SymTabEntry) actualNode.getAttribute(ID);
                sendAssignMessage(callNode, actualId.getName(), value);
            }
        }
        if (routineCode == READLN) {
            try {
                standardIn.skipToNextLine();
            } catch (Exception ex) {
                errorHandler.flag(callNode, INVALID_INPUT, this);
            }
        }
        return null;
    }

    private Object parseNumber(Token token, TypeSpec baseType) throws Exception {
        TokenType type = token.getType();
        TokenType sign = null;

        if (type == PLUS || type == MINUS) {
            sign = type;
            token = standardIn.nextToken();
            type = token.getType();
        }

        if (type == INTEGER) {
            Number value = sign == MINUS
                    ? -((Integer) token.getValue())
                    : (Integer) token.getValue();
            return baseType == Predefined.integerType ? value : Float.valueOf(((Integer) value).intValue());
        } else if (type == REAL) {
            Number value = sign == MINUS
                    ? -((Float) token.getValue())
                    : (Float) token.getValue();
            return type == Predefined.realType ? value : Integer.valueOf(((Integer) value).intValue());
        } else {
            throw new Exception();
        }
    }

    private Object parseBoolean(Token token) throws Exception {
        if (token.getType() == IDENTIFIER) {
            String text = token.getText();

            if (text.equalsIgnoreCase("true")) {
                return Boolean.valueOf(true);
            } else if (text.equalsIgnoreCase("false")) {
                return Boolean.valueOf(false);
            } else {
                throw new Exception();
            }
        } else {
            throw new Exception();
        }
    }

    private Object executeWriteWriteln(ICodeNode callNode, RoutineCode routineCode) {
        ICodeNode paramsNode = callNode.getChildren().size() > 0
                ? callNode.getChildren().get(0)
                : null;
        if (paramsNode != null) {
            ArrayList<ICodeNode> actuals = paramsNode.getChildren();

            for (ICodeNode writeParamsNode : actuals) {
                ArrayList<ICodeNode> children = writeParamsNode.getChildren();
                ICodeNode exprNode = children.get(0);
                TypeSpec dataType = exprNode.getTypeSpec().baseType();
                String typeCode =
                          dataType.isPascalString() ? "s"
                        : dataType == Predefined.integerType ? "d"
                        : dataType == Predefined.realType ? "f"
                        : dataType == Predefined.booleanType ? "s"
                        : dataType == Predefined.charType ? "c"
                        : "s";
                Object value = expressionExecutor.execute(exprNode);

                if (dataType == Predefined.charType && value instanceof String) {
                    value = ((String) value).charAt(0);
                }
                StringBuilder format = new StringBuilder("%");

                // Process any field width and precision values
                if (children.size() > 1) {
                    int w = (Integer) children.get(1).getAttribute(VALUE);
                    format.append(w == 0 ? 1 : w);
                }
                if (children.size() > 2) {
                    int p = (Integer) children.get(1).getAttribute(VALUE);
                    format.append(".");
                    format.append(p == 0 ? 1 : p);
                }

                format.append(typeCode);

                standardOut.printf(format.toString(), value);
                standardOut.flush();
            }
        }
        if (routineCode == WRITELN) {
            standardOut.println();
            standardOut.flush();
        }

        return null;
    }

    private Boolean executeEofEoln(ICodeNode callNode, RoutineCode routineCode) {
        try {
            if (routineCode == EOF) {
                return standardIn.atEof();
            } else {
                return standardIn.atEol();
            }
        } catch (Exception e) {
            errorHandler.flag(callNode, INVALID_INPUT, this);
            return true;
        }
    }

    private Number executeAbsSqr(ICodeNode callNode, RoutineCode routineCode, ICodeNode actualNode) {
       Object argValue = expressionExecutor.execute(actualNode);
       if (argValue instanceof  Integer) {
           int value = (Integer) argValue;
           return routineCode == ABS ? Math.abs(value) : value*value;
       } else {
           float value = (Float) argValue;
           return routineCode == ABS ? Math.abs(value) : value*value;
       }
    }

    private Float executeArctanCosExpLnSinSqrt(ICodeNode callNode, RoutineCode routineCode, ICodeNode actualNode) {
        Object argValue = expressionExecutor.execute(actualNode);
        float value = argValue instanceof Integer ? (Integer) argValue : (Float) argValue;

        switch ((RoutineCodeImpl) routineCode) {
            case ARCTAN: return (float) Math.atan(value);
            case COS: return (float) Math.cos(value);
            case EXP: return (float) Math.exp(value);
            case SIN: return (float) Math.sin(value);

            case LN: {
                if (value > 0.0f) {
                    return (float) Math.log(value);
                } else {
                    errorHandler.flag(callNode, INVALID_STANDARD_FUNCTION_ARGUMENT, this);
                    return 0.0f;
                }
            }
            case SQRT: {
                if (value >= 0.0f) {
                    return (float) Math.sqrt(value);
                } else {
                    errorHandler.flag(callNode, INVALID_STANDARD_FUNCTION_ARGUMENT, this);
                    return 0.0f;
                }
            }
            default: return 0.0f; // Should never get here
        }
    }

    private Integer executePredSucc(ICodeNode callNode, RoutineCode routineCode, ICodeNode actualNode, TypeSpec type) {
        int value = (Integer) expressionExecutor.execute(actualNode);
        int newValue = routineCode == PRED ? --value : ++value;

        return (Integer) checkRange(callNode, type, newValue);
    }

    private Character executeChr(ICodeNode callNode, RoutineCode routineCode, ICodeNode actualNode) {
        int value = (Integer) expressionExecutor.execute(actualNode);
        return (char) value;
    }

    private Boolean executeOdd(ICodeNode callNode, RoutineCode routineCode, ICodeNode actualNode) {
        int value = (Integer) expressionExecutor.execute(actualNode);
        return (value & 1)  == 1;
    }

    private Integer excuteOrd(ICodeNode callNode, RoutineCode routineCode, ICodeNode actualNode) {
        Object value = expressionExecutor.execute(actualNode);
        if (value instanceof Character) {
            char ch = ((Character) value).charValue();
            return (int) ch;
        } else if (value instanceof String) {
            char ch = ((String) value).charAt(0);
            return (int) ch;
        } else return (Integer) value;
    }

    private Integer executeRoundTrunc(ICodeNode callNode, RoutineCode routineCode, ICodeNode actualNode) {
        float value = (Float) expressionExecutor.execute(actualNode);
         if (routineCode == ROUND) {
             return value >= 0.0f
                     ? (int) (value + 0.5f)
                     : (int) (value - 0.5f);
         } else return (int) value;
    }

}
