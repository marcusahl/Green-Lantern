package wci.intermediate;

import wci.intermediate.symtabimpl.*;

public class SymTabFactory 
{

	public static SymTabStack createSymTabStack()
	{
		return new SymTabStackImpl();
	}
	
	public static SymTab createSymTab(int nestingLevel)
	{
		return new SymTabImpl(nestingLevel);
	}
	
	public static SymTabEntry createSymTabEntry(String name, SymTab symtab)
	{
		return new SymTabEntryImpl(name, symtab);
	}
	
}
