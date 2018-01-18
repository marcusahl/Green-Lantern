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

	public SymTabImpl(int nestingLevel) 
	{
		this.nestingLevel = nestingLevel;
	}

	public int getNestingLevel() 
	{
		return nestingLevel;
	}

	public SymTabEntry enter(String name) 
	{
		SymTabEntry entry = SymTabFactory.createSymTabEntry(name, this);
		put(name, entry);
		
		return entry;
	}

	public SymTabEntry lookup(String name) 
	{
		return get(name);
	}

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
