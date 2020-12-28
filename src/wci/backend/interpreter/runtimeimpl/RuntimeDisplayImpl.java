package wci.backend.interpreter.runtimeimpl;

import wci.backend.interpreter.ActivationRecord;
import wci.backend.interpreter.RuntimeDisplay;

import java.util.ArrayList;

public class RuntimeDisplayImpl extends ArrayList<ActivationRecord> implements RuntimeDisplay {

    public RuntimeDisplayImpl() {
        add(null); // dummy element 0 (never used)
    }

    @Override
    public ActivationRecord getActivationRecord(int nestingLevel) {
        return get(nestingLevel);
    }

    @Override
    public void callUpdate(int nestingLevel, ActivationRecord ar) {
        if (nestingLevel >= size()) {
            add(ar);
        }
        else {
            ActivationRecord prevAr = get(nestingLevel);
            set(nestingLevel, ar.setPredecessorRecord(prevAr));
        }
    }

    @Override
    public void returnUpdate(int nestingLevel) {
        int topIndex = size() - 1;
        ActivationRecord ar = get(nestingLevel);
        ActivationRecord prevAr = ar.getPredecessorRecord();

        if (prevAr != null) {
            set(nestingLevel, prevAr);
        } else if (nestingLevel == topIndex) {
            remove(topIndex);
        }
    }
}
