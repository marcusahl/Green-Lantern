package wci.backend.interpreter.memorymapimpl;

import wci.backend.interpreter.*;
import wci.backend.interpreter.activationrecordimpl.ActivationRecordImpl;
import wci.backend.interpreter.cellimpl.CellImpl;
import wci.backend.interpreter.runtimeimpl.RuntimeDisplayImpl;
import wci.backend.interpreter.runtimeimpl.RuntimeStackImpl;
import wci.intermediate.SymTab;
import wci.intermediate.SymTabEntry;

public class MemoryFactory {

    public static RuntimeStack createRuntimeStack() {
        return new RuntimeStackImpl();
    }

    public static RuntimeDisplay createRuntimeDisplay() {
        return new RuntimeDisplayImpl();
    }

    public static ActivationRecord createActivationRecord(SymTabEntry routineId) {
        return new ActivationRecordImpl(routineId);
    }

    public static MemoryMap createMemoryMap(SymTab symTab) {
        return new MemoryMapImpl(symTab);
    }

    public static Cell createCell(Object value) {
        return new CellImpl(value);
    }
}
