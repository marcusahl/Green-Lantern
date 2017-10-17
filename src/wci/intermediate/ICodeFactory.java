package wci.intermediate;

import wci.intermediate.icodeimpl.*;;

public class ICodeFactory {
	

	public static ICode createICode()
	{
		return new ICodeImpl();
	}
	
	public static ICodeNode createICodeNode(ICodeNodeType type)
	{
		return new ICodeNodeImpl(type);
	}

}
