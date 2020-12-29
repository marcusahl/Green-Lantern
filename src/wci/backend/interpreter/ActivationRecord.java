package wci.backend.interpreter;

import wci.intermediate.SymTabEntry;

import java.util.ArrayList;

public interface ActivationRecord {
    SymTabEntry getRoutineId();
    Cell getCell(String name);
    ArrayList<String> getAllNames();
    int getNestingLevel();
    ActivationRecord getPredecessorRecord();
    ActivationRecord setPredecessorRecord(ActivationRecord ar);
}
