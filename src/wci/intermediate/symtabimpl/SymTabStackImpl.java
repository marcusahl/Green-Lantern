package wci.intermediate.symtabimpl;

import java.util.ArrayList;

import wci.intermediate.*;

public class SymTabStackImpl 
	extends ArrayList<SymTab> 
	implements SymTabStack 
{
	private int currentNestingLevel;
	private SymTabEntry programId;
	
	public SymTabStackImpl() 
	{
		this.currentNestingLevel = 0;
		add(SymTabFactory.createSymTab(currentNestingLevel));
	}
	
	public int getCurrentNestingLevel() 
	{
		return currentNestingLevel;
	}

	public SymTab getLocalSymTab() 
	{
		return get(currentNestingLevel);
	}

	public SymTabEntry enterLocal(String name) 
	{
		return get(currentNestingLevel).enter(name);
	}

	public SymTabEntry lookupLocal(String name) 
	{
		return get(currentNestingLevel).lookup(name);
	}

	public void setProgramId(SymTabEntry entry) {
		programId = entry;
		
	}

	public SymTabEntry getProgramId() {
		return programId;
		
	}

	public SymTab push() {
		SymTab symTab = SymTabFactory.createSymTab(++currentNestingLevel);
		add(symTab);
		
		return symTab;
	}

	public SymTab push(SymTab symTab) {
		++currentNestingLevel;
		add(symTab);
		
		return symTab;
	}

	public SymTab pop() {
		SymTab symTab = get(currentNestingLevel);
		remove(currentNestingLevel--);
		
		return symTab;
	}
	
	public SymTabEntry lookup(String name) 
	{
		SymTabEntry foundEntry = null;
		
		for (int i = currentNestingLevel; (i >= 0) && (foundEntry == null); --i) {
			foundEntry = get(i).lookup(name);
		}
		return foundEntry;
	}

}
