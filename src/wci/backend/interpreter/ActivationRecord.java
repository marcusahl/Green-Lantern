package wci.backend.interpreter;

import wci.intermediate.SymTabEntry;

import java.util.ArrayList;

public interface ActivationRecord {
    public SymTabEntry getRoutineId();
    public Cell getCell(String name);
    public ArrayList getAllNames();
    public int getNestingLevel();
    public ActivationRecord getPredecessorRecord();
    public ActivationRecord setPredecessorRecord(ActivationRecord ar);
}
