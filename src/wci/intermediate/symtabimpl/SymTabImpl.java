package wci.intermediate.symtabimpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;

import wci.intermediate.*;


public class SymTabImpl 
	extends TreeMap<String, SymTabEntry> 
	implements SymTab 
{
	private int nestingLevel;

	/**
	 * Constructor
	 * @param nestingLevel the nesting level.
	 */
	public SymTabImpl(int nestingLevel) 
	{
		this.nestingLevel = nestingLevel;
	}

	/**
	 * Getter
	 * @return the nesting level
	 */
	public int getNestingLevel() 
	{
		return nestingLevel;
	}

	/**
	 * Create and enter a new entry into the symbol table.
	 * @param name the name of the entry.
	 * @return the new entry.
	 */
	public SymTabEntry enter(String name) 
	{
		SymTabEntry entry = SymTabFactory.createSymTabEntry(name, this);
		put(name, entry);
		
		return entry;
	}

	/**
	 * Look up an existing symbol table entry.
	 * @param name the name of the entry.
	 * @return the entry, or null if it does not exist.
	 */
	public SymTabEntry lookup(String name) 
	{
		return get(name);
	}

	/**
	 * Returns a list of symbol table entries sorted by name
	 * @return sorted list of symbol table entries.
	 */
	public ArrayList<SymTabEntry> sortedEntries() 
	{
		Collection<SymTabEntry> entries = values();
		Iterator<SymTabEntry> iter = entries.iterator();
		ArrayList<SymTabEntry> list = new ArrayList<SymTabEntry>(size());
		
		// Iterate over the sorted entries and append them to the list
		while (iter.hasNext())
		{
			list.add(iter.next());
		}
		
		return list;				// sorted list of entries
		
	}

}
