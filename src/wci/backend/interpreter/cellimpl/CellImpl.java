package wci.backend.interpreter.cellimpl;

import wci.backend.interpreter.Cell;

public class CellImpl implements Cell {

    private Object value = null;

    public CellImpl(Object value) {
        this.value = value;
    }

    @Override
    public void setValue(Object newValue) {
        value = newValue;
    }

    @Override
    public Object getValue() {
        return value;
    }
}
