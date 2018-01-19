package wci.intermediate.typeimpl;

import java.util.HashMap;

import wci.intermediate.*;
import wci.intermediate.symtabimpl.Predefined;

import static wci.intermediate.typeimpl.TypeFormImpl.*;
import static wci.intermediate.typeimpl.TypeKeyImpl.*;


public class TypeSpecImpl 
	extends HashMap<TypeKey, Object> 
	implements TypeSpec 
{
	private TypeForm form;
	private SymTabEntry identifier;

	public TypeSpecImpl(TypeForm form) {
		
		this.form = form;
		this.identifier = null;
	}
	
	public TypeSpecImpl(String value) {
		
		this.form = ARRAY;
		
		TypeSpec indexType = new TypeSpecImpl(SUBRANGE);
		indexType.setAttribute(SUBRANGE_MIN_VALUE, Predefined.integerType);
		indexType.setAttribute(SUBRANGE_MAX_VALUE, value.length());
		
		setAttribute(ARRAY_INDEX_TYPE, indexType);
		setAttribute(ARRAY_ELEMENT_TYPE, Predefined.charType);
		setAttribute(ARRAY_ELEMENT_COUNT, value.length());
	}
	
	
	public void setAttribute(TypeKey key, Object value) {
		
		this.put(key, value);
	}
	
	public Object getAttribute(TypeKey key) {
		return this.get(key);
	}
	
	public boolean isPascalString() {
		if (form == ARRAY) {
			TypeSpec elementType = (TypeSpec) getAttribute(ARRAY_ELEMENT_TYPE);
			TypeSpec indexType = (TypeSpec) getAttribute(ARRAY_INDEX_TYPE);
			
			return (elementType.baseType() == Predefined.charType) &&
				   (indexType.baseType() == Predefined.integerType);
		}
		else {
			return false;
		}
	}
	

	public TypeSpec baseType() {
		return (TypeFormImpl) form == SUBRANGE
				? (TypeSpec) getAttribute(SUBRANGE_BASE_TYPE)
				: this;
	}
	
	public TypeForm getForm() {

		return form;
	}

	@Override
	public void setIdentifier(SymTabEntry identifier) {
		// TODO Auto-generated method stub

	}

	@Override
	public SymTabEntry getIdentifier() {
		return identifier;
	}




}
