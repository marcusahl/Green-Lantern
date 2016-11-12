package wci.intermediate;

import java.util.ArrayList;

/**
 * <h1>SymTab</h1>
 * 
 * <p>The framework interface that represents the symbol table.</p>
 */

public interface SymTab 
{
	/**
	 * Getter
	 * @return the scope nesting level of this entry.
	 */
	public int getNestingLevel();
	
	/**
	 * Enter a new entry in the symbol table.
	 * @param name the name of the entry.
	 * @return the new entry
	 */
	public SymTabEntry enter(String name);
	
	/**
	 * Look up for an existing entry in the symbol table.
	 * @param name the name of the entry.
	 * @return the entry, or null if it does not exist.
	 */
	public SymTabEntry lookup(String name);
	
	/**
	 * @return a list of symbol table entries sorted by name.
	 */
	public ArrayList<SymTabEntry> sortedEntries();
}
