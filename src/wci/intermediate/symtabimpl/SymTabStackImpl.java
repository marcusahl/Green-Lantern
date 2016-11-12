package wci.intermediate.symtabimpl;

import java.util.ArrayList;

import wci.intermediate.*;

public class SymTabStackImpl 
	extends ArrayList<SymTab> 
	implements SymTabStack 
{
	private int currentNestingLevel;

	/**
	 * Constructor.
	 */
	public SymTabStackImpl() 
	{
		this.currentNestingLevel = 0;
		add(SymTabFactory.createSymTab(currentNestingLevel));
	}

	/**
	 * Getter
	 * @return current nesting level.
	 */
	public int getCurrentNestingLevel() 
	{
		return currentNestingLevel;
	}

	/**
	 * Returns the local symbol table which is at the top of the stack.
	 * @return the local symbol table.
	 */
	public SymTab getLocalSymTab() 
	{
		return get(currentNestingLevel);
	}

	/**
	 * Create a new entry in the local symbol table.
	 * @param name the name of the entry.
	 * @return the new entry.
	 */
	public SymTabEntry enterLocal(String name) 
	{
		return get(currentNestingLevel).enter(name);
	}

	/**
	 * Look up an entry in the local symbol table.
	 * @param name the name of the entry.
	 * @return the entry, null if it does not exist.
	 */
	public SymTabEntry lookupLocal(String name) 
	{
		return get(currentNestingLevel).lookup(name);
	}
	
	/**
	 * Look up an existing symbol table entry throughout the stack.
	 * @param name the name of the entry.
	 * @return the entry, or null if it does not exist.
	 */
	public SymTabEntry lookup(String name) 
	{
		return lookupLocal(name);
	}

}
