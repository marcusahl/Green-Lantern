package wci.intermediate.symtabimpl;

import static wci.intermediate.symtabimpl.SymTabKeyImpl.*;
import static wci.intermediate.typeimpl.TypeFormImpl.*;
import static wci.intermediate.typeimpl.TypeKeyImpl.*;

import java.util.ArrayList;

import wci.intermediate.SymTabEntry;
import wci.intermediate.SymTabStack;
import wci.intermediate.TypeFactory;
import wci.intermediate.TypeSpec;

public class Predefined {

	// Predefined types
	public static TypeSpec integerType;
	public static TypeSpec realType;
	public static TypeSpec booleanType;
	public static TypeSpec charType;
	public static TypeSpec undefinedType;
	
	// Predefined identifier
	public static SymTabEntry integerId;
	public static SymTabEntry realId;
	public static SymTabEntry booleanId;
	public static SymTabEntry charId;
	public static SymTabEntry falseId;
	public static SymTabEntry trueId;
	
	public static void initialize(SymTabStack symTabStack) {
		initializeTypes(symTabStack);
		initializeConstants(symTabStack);
	}
	
	private static void initializeTypes(SymTabStack symTabStack) {
		
		// Integer type
		integerId = symTabStack.enterLocal("integer");
		integerType = TypeFactory.createType(SCALAR);
		integerType.setIdentifier(integerId);
		integerId.setDefinition(DefinitionImpl.TYPE);
		integerId.setTypeSpec(integerType);
		
		// Real type
		realId = symTabStack.enterLocal("real");
		realType = TypeFactory.createType(SCALAR);
		realType.setIdentifier(realId);
		realId.setDefinition(DefinitionImpl.TYPE);
		realId.setTypeSpec(realType);
		
		// Boolean type
		booleanId = symTabStack.enterLocal("boolean");
		booleanType = TypeFactory.createType(ENUMERATION);
		booleanType.setIdentifier(booleanId);
		booleanId.setDefinition(DefinitionImpl.TYPE);
		booleanId.setTypeSpec(booleanType);
		
		// Character type
		charId = symTabStack.enterLocal("char");
		charType = TypeFactory.createType(SCALAR);
		charType.setIdentifier(charId);
		charId.setDefinition(DefinitionImpl.TYPE);
		charId.setTypeSpec(charType);
		
		// Undefined type
		undefinedType = TypeFactory.createType(SCALAR);
	}
	
	private static void initializeConstants(SymTabStack symTabStack) {
		
		// Boolean enumeration constant false
		falseId = symTabStack.enterLocal("false");
		falseId.setDefinition(DefinitionImpl.ENUMERATION_CONSTANT);
		falseId.setTypeSpec(booleanType);
		falseId.setAttribute(CONSTANT_VALUE, new Integer(0));
		
		// Boolean enumeration constant true
		trueId = symTabStack.enterLocal("true");
		trueId.setDefinition(DefinitionImpl.ENUMERATION_CONSTANT);
		trueId.setTypeSpec(booleanType);
		trueId.setAttribute(CONSTANT_VALUE, new Integer(1));
		
		// Add true and false to the boolean enumeration type
		ArrayList<SymTabEntry> constants = new ArrayList<SymTabEntry>();
		constants.add(falseId);
		constants.add(trueId);
		booleanType.setAttribute(ENUMERATION_CONSTANTS, constants);
		
	}

}