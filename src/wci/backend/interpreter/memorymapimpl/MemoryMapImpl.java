package wci.backend.interpreter.memorymapimpl;

import wci.backend.interpreter.Cell;
import wci.backend.interpreter.MemoryMap;
import wci.intermediate.*;
import wci.intermediate.typeimpl.TypeFormImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import static wci.intermediate.symtabimpl.DefinitionImpl.*;
import static wci.intermediate.typeimpl.TypeKeyImpl.*;

public class MemoryMapImpl extends HashMap<String, Cell> implements MemoryMap {

    public MemoryMapImpl(SymTab symTab) {
        ArrayList<SymTabEntry> entries = symTab.sortedEntries();

        for (SymTabEntry entry: entries) {
            Definition defn = entry.getDefinition();
            if (defn == VARIABLE || defn == FUNCTION || defn == VALUE_PARM || defn == FIELD) {
                String name = entry.getName();
                TypeSpec type = entry.getTypeSpec();
                put(name, MemoryFactory.createCell(allocateCellValue(type)));
            } else if (defn == VAR_PARM) {
                String name = entry.getName();
                put(name, MemoryFactory.createCell(null));
            }
        }
    }

    private Object allocateCellValue(TypeSpec type) {
        TypeForm form = type.getForm();

        switch ((TypeFormImpl) form) {
            case ARRAY: {
                return allocateArrayCells(type);
            }
            case RECORD: {
                return allocateRecordMap(type);
            }
            default: {
                return null; // uninitialized scalar value
            }
        }
    }

    private Object[] allocateArrayCells(TypeSpec type) {
        int elementCount = (Integer) type.getAttribute(ARRAY_ELEMENT_COUNT);
        TypeSpec elementType = (TypeSpec) type.getAttribute(ARRAY_ELEMENT_TYPE);
        Cell allocation[] = new Cell[elementCount];

        for (int i = 0; i < elementCount; i++) {
            allocation[i] = MemoryFactory.createCell(allocateCellValue(elementType));
        }
        return allocation;
    }

    private MemoryMap allocateRecordMap(TypeSpec type) {
        SymTab symTab = (SymTab) type.getAttribute(RECORD_SYMTAB);
        return MemoryFactory.createMemoryMap(symTab);
    }

    @Override
    public Cell getCell(String name) {
        return get(name);
    }

    @Override
    public ArrayList<String> getAllNames() {
        ArrayList<String> list = new ArrayList<String>();
        Set<String> names = keySet();
        Iterator<String> it = names.iterator();

        while(it.hasNext()) {
            list.add(it.next());
        }

        return list;
    }
}
