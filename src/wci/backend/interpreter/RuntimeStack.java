package wci.backend.interpreter;

import java.util.ArrayList;

public interface RuntimeStack {
    public ArrayList<ActivationRecord> records();
    public ActivationRecord getTopMost(int nestingLevel);
    public int currentNestingLevel();
    public void push(ActivationRecord ar);
    public void pop();
}
