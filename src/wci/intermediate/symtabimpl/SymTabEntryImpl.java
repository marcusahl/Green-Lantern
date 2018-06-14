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
	private Definition definition;
	private TypeSpec typeSpec;

	public SymTabEntryImpl(String name, SymTab symTab) 
	{
		this.name = name;
		this.parentSymTab = symTab;
		this.lineNumbers = new ArrayList<Integer>();
		this.definition = null;
		this.typeSpec = null;
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

	
	public void setDefinition(Definition definition) {
		this.definition = definition;
	}

	public Definition getDefinition() {
		return definition;
	}

	public void setTypeSpec(TypeSpec typeSpec) {
		this.typeSpec = typeSpec;
	}

	public TypeSpec getTypeSpec() {
		return typeSpec;
	}

}
