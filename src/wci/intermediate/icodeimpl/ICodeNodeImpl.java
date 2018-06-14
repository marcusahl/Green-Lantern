package wci.intermediate.icodeimpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import wci.intermediate.*;

public class ICodeNodeImpl 
	extends HashMap<ICodeKey, Object> 
	implements ICodeNode 
{
	private ICodeNodeType type;
	private TypeSpec typeSpec;
	private ICodeNode parent;					
	private ArrayList<ICodeNode> children;		
	
	public ICodeNodeImpl(ICodeNodeType type)
	{
		this.type = type;
		this.parent = null;
		this.children = new ArrayList<ICodeNode>();
		this.typeSpec = null;
	}

	public ICodeNodeType getType() 
	{
		return type;
	}

	public ICodeNode getParent() 
	{
		return parent;
	}

	public ICodeNode addChild(ICodeNode node) 
	{
		if (node != null)
		{
			children.add(node);
			((ICodeNodeImpl) node).parent = this; 
		}
		
		return node;
	}

	public ArrayList<ICodeNode> getChildren() 
	{
		return children;
	}

	public void setAttribute(ICodeKey key, Object value) 
	{
		put(key,value);
	}

	public Object getAttribute(ICodeKey key) 
	{
		return get(key);
	}


	public ICodeNode copy() 
	{
		// Create a copy with the same type.
		ICodeNodeImpl copy = (ICodeNodeImpl) ICodeFactory.createICodeNode(type);
		
		Set<Map.Entry<ICodeKey, Object>> attributes = entrySet();
		Iterator<Map.Entry<ICodeKey, Object>> it = attributes.iterator();
		
		// Copy attributes
		while (it.hasNext())
		{
			Map.Entry<ICodeKey, Object> attribute = it.next();
			copy.put(attribute.getKey(), attribute.getValue());
		}
		
		return copy;
	}
	
	public String toString()
	{
		return type.toString();
	}

	public void setTypeSpec(TypeSpec typeSpec) {
		this.typeSpec = typeSpec;
		
	}

	public TypeSpec getTypeSpec() {
		return typeSpec;
	}

}
