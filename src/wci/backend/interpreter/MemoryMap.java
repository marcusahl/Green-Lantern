package wci.backend.interpreter;

import java.util.ArrayList;

public interface MemoryMap {
    public Cell getCell(String name);
    public ArrayList<String> getAllNames();
}
