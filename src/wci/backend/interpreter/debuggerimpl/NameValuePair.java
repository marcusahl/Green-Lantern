package wci.backend.interpreter.debuggerimpl;

import wci.backend.interpreter.Cell;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class NameValuePair {
    private String variableName;
    private String valueString;

    private static final int MAX_DISPLAYED_ELEMENTS = 10;

    public NameValuePair(String variableName, Object value) {
        this.variableName = variableName;
        this.valueString = valueString(value);
    }

    public String getVariableName() {
        return variableName;
    }
    public String getValueString() {
        return valueString;
    }

    protected static String valueString(Object value) {
        StringBuilder buffer = new StringBuilder();

        if (value == null) buffer.append("?");
        else if (value instanceof Cell) buffer.append(valueString(((Cell) value).getValue()));  // Dereference a VAR parameter
        else if (value instanceof Cell[]) arrayValueString((Cell[]) value, buffer); // Array value
        else if (value instanceof HashMap) recordValueString((HashMap) value, buffer); // Record value
        else if (value instanceof Character) buffer.append("'").append(value).append("'"); // Character value
        else buffer.append(value.toString()); // Numeric value
        return buffer.toString();
    }

    private static void arrayValueString(Cell value[], StringBuilder buffer) {
        int elementCount = 0;
        boolean first = true;
        buffer.append("[");

        for (Cell cell : value) {
            if (first) first = false;
            else buffer.append(", ");

            if (++elementCount <= MAX_DISPLAYED_ELEMENTS) buffer.append(valueString(cell.getValue()));

            else {
                buffer.append("...");
                break;
            }
        }
        buffer.append("]");
    }

    private static void recordValueString(HashMap value, StringBuilder buffer) {
        boolean first = true;
        buffer.append("{");

        Set<Map.Entry<String, Cell>> entries = value.entrySet();
        Iterator<Map.Entry<String, Cell>> it = entries.iterator();

        while (it.hasNext()) {
            Map.Entry<String, Cell> entry = it.next();
            if (first) first = false;
            else buffer.append(", ");
            buffer.append(valueString(entry.getValue().getValue()));
        }

        buffer.append("}");
    }

}
