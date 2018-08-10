package wci.frontend.pascal.parsers;

import static wci.intermediate.icodeimpl.ICodeKeyImpl.*;
import static wci.intermediate.icodeimpl.ICodeNodeTypeImpl.CALL;

import wci.frontend.Token;
import wci.frontend.pascal.PascalParserTD;
import wci.intermediate.ICodeFactory;
import wci.intermediate.ICodeNode;
import wci.intermediate.SymTabEntry;

public class CallDeclaredParser extends CallParser {

    public ICodeNode parse(Token token) throws Exception {
        ICodeNode callNode = ICodeFactory.createICodeNode(CALL);
        SymTabEntry routineId = symTabStack.lookup(token.getText().toLowerCase());
        callNode.setAttribute(ID, routineId);
        callNode.setTypeSpec(routineId.getTypeSpec());

        token = nextToken();

        ICodeNode paramsNode = parseActualParameters(token, routineId, true, false, false);
        callNode.addChild(paramsNode);
        return callNode;
    }

    public CallDeclaredParser(PascalParserTD parent) {
        super(parent);
    }

}
