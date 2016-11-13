package wci.intermediate.icodeimpl;

import wci.intermediate.ICode;
import wci.intermediate.ICodeNode;

/**
 * <h1>ICodeImpl</h1>
 * 
 *<p>An implementation of the intermediate code as a parse tree.</p>
 */
public class ICodeImpl 
	implements ICode 
{
	private ICodeNode root;							// root node
	
	/**
	 * Constructor
	 */
	public ICodeImpl()
	{
		root = null;
	}
	
	/**
	 * Set and return the root node.
	 * @param node the root node.
	 * @return the root node.
	 */
	public ICodeNode setRoot(ICodeNode node) 
	{
		root = node;
		return root;
	}

	/**
	 * Getter
	 * @return root node
	 */
	public ICodeNode getRoot() 
	{
		return root;
	}

}
