package wci.intermediate.icodeimpl;

import wci.intermediate.ICode;
import wci.intermediate.ICodeNode;


public class ICodeImpl 
	implements ICode 
{
	private ICodeNode root;							// root node
	
	public ICodeImpl()
	{
		root = null;
	} 

	public ICodeNode setRoot(ICodeNode node) 
	{
		root = node;
		return root;
	}

	public ICodeNode getRoot() 
	{
		return root;
	}

}
