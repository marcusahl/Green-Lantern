package wci.intermediate.symtabimpl;

import java.util.ArrayList;
import java.util.HashMap;

import wci.intermediate.*;

public class SymTabEntryImpl 
	extends HashMap<SymTabKey, Object> 
	implements SymTabEntry 
{
	
	private String name;									
	private SymTab parentSymTab;								
	private ArrayList<Integer> lineNumbers;				

	public SymTabEntryImpl(String name, SymTab symTab) 
	{
		this.name = name;
		this.parentSymTab = symTab;
		this.lineNumbers = new ArrayList<Integer>();
	}

	public String getName() 
	{
		return name;
	}

	public SymTab getSymTab() 
	{
		return parentSymTab;
	}

	public void appendLineNumber(int lineNumber) 
	{
		this.lineNumbers.add(lineNumber);
	}

	public ArrayList<Integer> getLineNumbers() {
		return lineNumbers;
	}

	public void setAttribute(SymTabKey key, Object value) 
	{
		put(key,value);
	}
	
	public Object getAttribute(SymTabKey key) {
		return get(key);
	}

	@Override
	public void setDefinition(Definition definition) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Definition getDefinition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTypeSpec(TypeSpec typeSpec) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public TypeSpec getTypeSpec() {
		// TODO Auto-generated method stub
		return null;
	}

}
