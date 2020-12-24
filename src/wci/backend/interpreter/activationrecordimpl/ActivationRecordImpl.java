package wci.backend.interpreter.activationrecordimpl;

import wci.backend.interpreter.ActivationRecord;
import wci.backend.interpreter.Cell;
import wci.backend.interpreter.MemoryMap;
import wci.backend.interpreter.memorymapimpl.MemoryFactory;
import wci.intermediate.SymTab;
import wci.intermediate.SymTabEntry;

import java.util.ArrayList;

import static wci.intermediate.symtabimpl.SymTabKeyImpl.ROUTINE_SYMTAB;

public class ActivationRecordImpl implements ActivationRecord {

    private SymTabEntry routineId;
    private ActivationRecord previousRecord;
    private int nestingLevel;
    private MemoryMap memoryMap;

    public ActivationRecordImpl(SymTabEntry routineId) {
        SymTab symTab = (SymTab) routineId.getAttribute(ROUTINE_SYMTAB);
        this.routineId = routineId;
        this.nestingLevel = symTab.getNestingLevel();
        this.memoryMap = MemoryFactory.createMemoryMap(symTab);
    }

    @Override
    public SymTabEntry getRoutineId() {
        return routineId;
    }

    @Override
    public Cell getCell(String name) {
        return memoryMap.getCell(name);
    }

    @Override
    public ArrayList getAllNames() {
        return memoryMap.getAllNames();
    }

    @Override
    public int getNestingLevel() {
        return nestingLevel;
    }

    @Override
    public ActivationRecord getPredecessorRecord() {
        return previousRecord;
    }

    @Override
    public ActivationRecord setPredecessorRecord(ActivationRecord ar) {
        previousRecord = ar;
        return this;
    }
}
