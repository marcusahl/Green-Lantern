package wci.intermediate;

import java.util.ArrayList;

/**
 * <h1>SymTabEntry</h1>
 * 
 *<p>The interface for a symbol table entry.</p>
 */

public interface SymTabEntry 
{
	/**
	 * Getter
	 * @return the name of the symbol table entry.
	 */
	public String getName();
	
	/**
	 * Getter
	 * @return the symbol table that contains this entry.
	 */
	public SymTab getSymTab();
	
	/**
	 * Stores the line number of a source line where the entry name appears.
	 * @param lineNumber the source line number.
	 */
	public void appendLineNumber(int lineNumber);
	
	/**
	 * Getter
	 * @return a list of source lines where the entry name appears.
	 */
	public ArrayList<Integer> getLineNumbers();
	
	/**
	 * Set the entry information in terms of attributes.
	 * @param key the attribute key.
	 * @param value the attribute value.
	 */
	public void setAttribute(SymTabKey key, Object value);
	
	/**
	 * Get the value of an attribute of the entry.
	 * @param key the attribute key.
	 * @return the entry's information in terms of attributes.
	 */
	public Object getAttribute(SymTabKey key);
}
