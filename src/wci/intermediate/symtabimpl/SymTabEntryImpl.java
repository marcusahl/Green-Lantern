package wci.intermediate.symtabimpl;

import java.util.ArrayList;
import java.util.HashMap;

import wci.intermediate.*;

public class SymTabEntryImpl 
	extends HashMap 
	implements SymTabEntry 
{
	
	private String name;									// entry name
	private SymTab symTab;									// parent symbol table
	private ArrayList<Integer> lineNumbers;					// source line numbers
	/**
	 * Constructor
	 * @param name the name of the entry.
	 * @param symTab the symbol table that contains this entry. 
	 */
	public SymTabEntryImpl(String name, SymTab symTab) 
	{
		this.name = name;
		this.symTab = symTab;
		this.lineNumbers = new ArrayList<Integer>();
	}

	/**
	 * Getter
	 * @return name the name of the entry.
	 */
	public String getName() 
	{
		return name;
	}

	/**
	 * Getter.
	 * @return the symbol table that the entry belongs too
	 */
	public SymTab getSymTab() 
	{
		return symTab;
	}

	/**
	 * Append a source line number 
	 */
	public void appendLineNumber(int lineNumber) 
	{
		this.lineNumbers.add(lineNumber);
	}

	/**
	 * Getter
	 * @return source line numbers of an entry.
	 */
	public ArrayList<Integer> getLineNumbers() {
		return lineNumbers;
	}

	/**
	 * Set an attribute of the entry.
	 * @param key the key of the attribute.
	 * @param value the attribute value.
	 */
	public void setAttribute(SymTabKey key, Object value) 
	{
		put(key,value);
	}
	

	/**
	 * Get the value of an attribute of the entry.
	 * @param key the attribute key.
	 * @return the attribute value
	 */
	public Object getAttribute(SymTabKey key) {
		return get(key);
	}

}
