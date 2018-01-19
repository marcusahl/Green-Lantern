package wci.intermediate.symtabimpl;

import wci.intermediate.Definition;

public enum DefinitionImpl implements Definition {
	CONSTANT, ENUMERATION_CONSTANT("enumeration constan"), TYPE, VARIABLE, FIELD("record field"),
	VALUE_PARM("value parameter"), VAR_PARM("VAR parameter"), PROGRAM_PARM("program parameter"),
	PROGRAM, PROCEDURE, FUNCTION, UNDEFINED;
	
	private String text;
	
	DefinitionImpl(){
		this.text = this.toString().toLowerCase();
	}
	
	DefinitionImpl(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
}
