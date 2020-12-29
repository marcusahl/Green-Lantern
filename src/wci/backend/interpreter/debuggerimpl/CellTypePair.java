package wci.backend.interpreter.debuggerimpl;

import wci.backend.interpreter.Cell;
import wci.backend.interpreter.Debugger;
import wci.frontend.pascal.PascalTokenType;
import wci.intermediate.SymTab;
import wci.intermediate.SymTabEntry;
import wci.intermediate.TypeForm;
import wci.intermediate.TypeSpec;
import wci.intermediate.typeimpl.TypeFormImpl;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;

import static wci.frontend.pascal.PascalTokenType.*;
import static wci.intermediate.typeimpl.TypeFormImpl.ENUMERATION;
import static wci.intermediate.typeimpl.TypeFormImpl.SUBRANGE;
import static wci.intermediate.typeimpl.TypeKeyImpl.*;

public class CellTypePair {

    private Cell cell;
    private TypeSpec type;
    private Debugger debugger;

    protected static final EnumSet<PascalTokenType> MODIFIER_SET = EnumSet.of(LEFT_BRACKET, DOT);

    protected CellTypePair(TypeSpec type, Cell cell, Debugger debugger) throws Exception {
        this.type = type;
        this.cell = cell;
        this.debugger = debugger;

        parseVariable();
    }

    public Cell getCell() { return cell; }
    public void setValue(Object newValue) {
        cell.setValue(newValue);
    }

    private void parseVariable() throws Exception {
        TypeForm form = type.getForm();
        Object value = cell.getValue();

        while(MODIFIER_SET.contains(debugger.currentToken().getType())) {
            if (form == TypeFormImpl.ARRAY) parseArrayVariable((Cell[]) value);
            else if (form == TypeFormImpl.RECORD) parseRecordVariable((HashMap) value);
            value = cell.getValue();
            form = type.getForm();
        }
    }

    private void parseArrayVariable(Cell[] value) throws Exception {
        debugger.nextToken();

        int index = debugger.getInteger("Integer index expected.");
        int minValue = 0;
        TypeSpec indexType = (TypeSpec) type.getAttribute(ARRAY_INDEX_TYPE);
        rangeCheck(index, indexType, "Index out of range.");
        type = (TypeSpec) type.getAttribute(ARRAY_ELEMENT_TYPE);

        if (indexType.getForm() == SUBRANGE) {
            minValue = (Integer) indexType.getAttribute(SUBRANGE_MIN_VALUE);
        }
        cell = value[index - minValue];

        if (debugger.currentToken().getType() == RIGHT_BRACKET) debugger.nextToken();
        else throw new Exception("] expected.");

    }

    private void rangeCheck(int value, TypeSpec type, String errorMessage) throws Exception {
        TypeForm form = type.getForm();
        Integer minValue = null;
        Integer maxValue = null;

        if (form == SUBRANGE) {
            minValue = (Integer) type.getAttribute(SUBRANGE_MIN_VALUE);
            maxValue = (Integer) type.getAttribute(SUBRANGE_MAX_VALUE);
        }
        else if (form == ENUMERATION) {
            ArrayList<SymTabEntry> constants = (ArrayList<SymTabEntry>) type.getAttribute(ENUMERATION_CONSTANTS);
            minValue = 0;
            maxValue = constants.size() - 1;
        }

        if (minValue != null && (value < minValue) || (value > maxValue)) {
            throw new Exception(errorMessage);
        }
    }

    private void parseRecordVariable(HashMap record) throws Exception {
        debugger.nextToken();
        String fieldName = debugger.getWord("Field name expected,");

        if (record.containsKey(fieldName)) cell = (Cell) record.get(fieldName);
        else throw new Exception("Invalid field name.");

        SymTab symTab = (SymTab) type.getAttribute(RECORD_SYMTAB);
        SymTabEntry id = symTab.lookup(fieldName);
        type = id.getTypeSpec();
    }
}
