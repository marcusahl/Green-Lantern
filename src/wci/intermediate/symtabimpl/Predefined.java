package wci.intermediate.symtabimpl;

import static wci.intermediate.symtabimpl.DefinitionImpl.PROCEDURE;
import static wci.intermediate.symtabimpl.RoutineCodeImpl.ABS;
import static wci.intermediate.symtabimpl.RoutineCodeImpl.ARCTAN;
import static wci.intermediate.symtabimpl.RoutineCodeImpl.CHR;
import static wci.intermediate.symtabimpl.RoutineCodeImpl.COS;
import static wci.intermediate.symtabimpl.RoutineCodeImpl.EOF;
import static wci.intermediate.symtabimpl.RoutineCodeImpl.EOLN;
import static wci.intermediate.symtabimpl.RoutineCodeImpl.EXP;
import static wci.intermediate.symtabimpl.RoutineCodeImpl.LN;
import static wci.intermediate.symtabimpl.RoutineCodeImpl.ODD;
import static wci.intermediate.symtabimpl.RoutineCodeImpl.ORD;
import static wci.intermediate.symtabimpl.RoutineCodeImpl.PRED;
import static wci.intermediate.symtabimpl.RoutineCodeImpl.READ;
import static wci.intermediate.symtabimpl.RoutineCodeImpl.READLN;
import static wci.intermediate.symtabimpl.RoutineCodeImpl.ROUND;
import static wci.intermediate.symtabimpl.RoutineCodeImpl.SIN;
import static wci.intermediate.symtabimpl.RoutineCodeImpl.SQR;
import static wci.intermediate.symtabimpl.RoutineCodeImpl.SQRT;
import static wci.intermediate.symtabimpl.RoutineCodeImpl.SUCC;
import static wci.intermediate.symtabimpl.RoutineCodeImpl.TRUNC;
import static wci.intermediate.symtabimpl.RoutineCodeImpl.WRITE;
import static wci.intermediate.symtabimpl.RoutineCodeImpl.WRITELN;
import static wci.intermediate.symtabimpl.SymTabKeyImpl.CONSTANT_VALUE;
import static wci.intermediate.symtabimpl.SymTabKeyImpl.ROUTINE_CODE;
import static wci.intermediate.typeimpl.TypeFormImpl.ENUMERATION;
import static wci.intermediate.typeimpl.TypeFormImpl.SCALAR;
import static wci.intermediate.typeimpl.TypeKeyImpl.ENUMERATION_CONSTANTS;

import java.util.ArrayList;

import wci.intermediate.Definition;
import wci.intermediate.RoutineCode;
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
	
	// Predefined procedures and functions
	public static SymTabEntry readId;
	public static SymTabEntry readlnId;
	public static SymTabEntry writeId;
	public static SymTabEntry writelnId;
	public static SymTabEntry absId;
	public static SymTabEntry arctanId;
	public static SymTabEntry chrId;
	public static SymTabEntry cosId;
	public static SymTabEntry eofId;
	public static SymTabEntry eolnId;
	public static SymTabEntry expId;
	public static SymTabEntry lnId;
	public static SymTabEntry oddId;
	public static SymTabEntry ordId;
	public static SymTabEntry preId;
	public static SymTabEntry roundId;
	public static SymTabEntry sinId;
	public static SymTabEntry sqrId;
	public static SymTabEntry sqrtId;
	public static SymTabEntry succId;
	public static SymTabEntry truncId;
	
	
	
	public static void initialize(SymTabStack symTabStack) {
		initializeTypes(symTabStack);
		initializeConstants(symTabStack);
		initializeStandardRoutines(symTabStack);
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
	
	private static void initializeStandardRoutines(SymTabStack symTabStack) {
		readId = enterStandardRoutine(symTabStack, PROCEDURE, "read", READ);
		readlnId = enterStandardRoutine(symTabStack, PROCEDURE, "readln", READLN);
		writeId = enterStandardRoutine(symTabStack, PROCEDURE, "write", WRITE);
		writelnId = enterStandardRoutine(symTabStack, PROCEDURE, "writeln", WRITELN);
		absId = enterStandardRoutine(symTabStack, PROCEDURE, "abs", ABS);
		arctanId = enterStandardRoutine(symTabStack, PROCEDURE, "arctan", ARCTAN);
		chrId = enterStandardRoutine(symTabStack, PROCEDURE, "chr", CHR);
		cosId = enterStandardRoutine(symTabStack, PROCEDURE, "cos", COS);
		eofId = enterStandardRoutine(symTabStack, PROCEDURE, "eof", EOF);
		eolnId = enterStandardRoutine(symTabStack, PROCEDURE, "eoln", EOLN);
		expId = enterStandardRoutine(symTabStack, PROCEDURE, "exp", EXP);
		lnId = enterStandardRoutine(symTabStack, PROCEDURE, "ln", LN);
		oddId = enterStandardRoutine(symTabStack, PROCEDURE, "odd", ODD);
		ordId = enterStandardRoutine(symTabStack, PROCEDURE, "ord", ORD);
		preId = enterStandardRoutine(symTabStack, PROCEDURE, "pre", PRED);
		roundId = enterStandardRoutine(symTabStack, PROCEDURE, "round", ROUND);
		sinId = enterStandardRoutine(symTabStack, PROCEDURE, "sin", SIN);
		sqrId = enterStandardRoutine(symTabStack, PROCEDURE, "sgr", SQR);
		sqrtId = enterStandardRoutine(symTabStack, PROCEDURE, "sqrt", SQRT);
		succId = enterStandardRoutine(symTabStack, PROCEDURE, "succ", SUCC);
		truncId = enterStandardRoutine(symTabStack, PROCEDURE, "trunc", TRUNC);
	}
	
	private static SymTabEntry enterStandardRoutine(
			SymTabStack symTabStack, 
			Definition definition, 
			String name,
			RoutineCode routineCode) 
	{
		SymTabEntry procId = symTabStack.enterLocal(name);
		procId.setDefinition(definition);
		procId.setAttribute(ROUTINE_CODE, routineCode);
		
		return procId;
	}

}
