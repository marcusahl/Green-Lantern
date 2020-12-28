package wci.backend.interpreter;

public interface RuntimeDisplay {
    public ActivationRecord getActivationRecord(int nestingLevel);
    public void callUpdate(int nestingLevel, ActivationRecord ar);
    public void returnUpdate(int nestingLevel);
}
