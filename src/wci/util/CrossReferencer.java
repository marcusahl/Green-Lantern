package wci.util;

import static wci.intermediate.symtabimpl.SymTabKeyImpl.*;
import static wci.intermediate.typeimpl.TypeFormImpl.*;
import static wci.intermediate.typeimpl.TypeKeyImpl.*;

import java.util.ArrayList;

import wci.intermediate.Definition;
import wci.intermediate.SymTab;
import wci.intermediate.SymTabEntry;
import wci.intermediate.SymTabStack;
import wci.intermediate.TypeForm;
import wci.intermediate.TypeSpec;
import wci.intermediate.symtabimpl.DefinitionImpl;
import wci.intermediate.typeimpl.TypeFormImpl;


public class CrossReferencer {

	private static final int NAME_WIDTH = 16;
	
	private static final String NAME_FORMAT = "%-" + NAME_WIDTH +"s";
	private static final String NUMBERS_LABEL = " Line numbers     ";
	private static final String NUMBERS_UNDERLINE = " ------------     ";
	private static final String NUMBER_FORMAT = " %03d";
	
	private static final int LABEL_WIDTH = NUMBERS_LABEL.length();
	private static final int INDENT_WIDTH = NAME_WIDTH + LABEL_WIDTH;
	
	private static final StringBuilder INDENT = new StringBuilder(INDENT_WIDTH);
	static 
	{
		for (int i = 0; i < INDENT_WIDTH; ++i)
			{
				INDENT.append(" ");
			}
	}
	
	private static final String ENUM_CONST_FORMAT = "%" + NAME_WIDTH + "s = %s";
	
	public void print(SymTabStack symTabStack)
	{
		System.out.println("\n===== CROSS-REFERENCES TABLE ====");
		SymTabEntry programId = symTabStack.getProgramId();
		printRoutine(programId);
	}
	
	private void printRoutine(SymTabEntry routineId) {
		Definition definition = routineId.getDefinition();
		System.out.println("\n*** " + definition.toString() + " " + routineId.getName() + "***");
		printColumnHeadings();		
		SymTab symTab = (SymTab) routineId.getAttribute(ROUTINE_SYMTAB);
		ArrayList<TypeSpec> newRecordTypes = new ArrayList<TypeSpec>();
		printSymTab(symTab, newRecordTypes);
		
		if (newRecordTypes.size() > 0) {
			printRecords(newRecordTypes);
		}
		
		ArrayList<SymTabEntry> routineIds = (ArrayList<SymTabEntry>) routineId.getAttribute(ROUTINE_ROUTINES);
		if (routineIds != null) {
			
			for (SymTabEntry rtnId : routineIds) {
				printRoutine(rtnId);
			}
		}
	}
	
	private void printColumnHeadings()
	{
		System.out.println();
		System.out.println(String.format(NAME_FORMAT, "Identifier") + NUMBERS_LABEL);
		System.out.println(String.format(NAME_FORMAT, "----------") + NUMBERS_UNDERLINE);
	}
	
	private void printSymTab(SymTab symTab, ArrayList<TypeSpec> recordTypes)
	{
		ArrayList<SymTabEntry> sorted = symTab.sortedEntries();
		for (SymTabEntry entry : sorted)
		{
			ArrayList<Integer> lineNumbers = entry.getLineNumbers();
			System.out.print(String.format(NAME_FORMAT, entry.getName()));
			if (lineNumbers != null)
			{
				for (Integer lineNumber : lineNumbers)
				{
					System.out.print(String.format(NUMBER_FORMAT, lineNumber));
				}
			}
			
			System.out.println();
			printEntry(entry, recordTypes);
		
		}
	}
	
	private void printEntry(SymTabEntry entry, ArrayList<TypeSpec> recordTypes) {
		Definition definition = entry.getDefinition();
		int nestingLevel = entry.getSymTab().getNestingLevel();
		System.out.println(INDENT + "Defined as: " + definition.toString());
		System.out.println(INDENT + "Scope nesting level: " + nestingLevel);
		
		TypeSpec type = entry.getTypeSpec();
		printType(type);
		
		switch ((DefinitionImpl) definition) {
		
			case CONSTANT: {
				Object value = entry.getAttribute(CONSTANT_VALUE);
				System.out.println(INDENT + "Value = " + toString(value));
				
				if (type.getIdentifier() == null) {
					printTypeDetail(type, recordTypes);
				}
				
				break;
			}
			
			case ENUMERATION_CONSTANT: {
				Object value = entry.getAttribute(CONSTANT_VALUE);
				System.out.println(INDENT + "Value = " + toString(value));
				
				break;
			}
			
			case TYPE: {
				
				if (entry == type.getIdentifier()) {
					printTypeDetail(type, recordTypes);
				}
				
				break;
			}
			
			case VARIABLE: {
				
				if (type.getIdentifier() == null) {
					printTypeDetail(type, recordTypes);
				}
				
				break;
			}
		}
	}
	
	private void printType(TypeSpec type) {
		
		if (type != null) {
			TypeForm form = type.getForm();
			SymTabEntry typeId = type.getIdentifier();
			String typeName = typeId != null ? typeId.getName() : "<unnamed>";
			System.out.println(INDENT + "Type form = " + form + ", Type id = " + typeName);
		}
	}
	
	private void printTypeDetail(TypeSpec type, ArrayList<TypeSpec> recordTypes) {
		TypeForm form = type.getForm();
		
		switch ((TypeFormImpl) form) {
			
			case ENUMERATION: {
				ArrayList<SymTabEntry> constantIds = (ArrayList<SymTabEntry>) 
						type.getAttribute(ENUMERATION_CONSTANTS);
				
				System.out.println(INDENT + "--- Enumeration constants ---");
				
				for (SymTabEntry constantId : constantIds) {
					String name = constantId.getName();
					Object value = constantId.getAttribute(CONSTANT_VALUE);
					
					System.out.println(INDENT + String.format(ENUM_CONST_FORMAT,  name, value));
				}
				
				break;
				
			}
			
			case SUBRANGE: {
				Object minValue = type.getAttribute(SUBRANGE_MIN_VALUE);
				Object maxValue = type.getAttribute(SUBRANGE_MAX_VALUE);
				TypeSpec baseTypeSpec = (TypeSpec) type.getAttribute(SUBRANGE_BASE_TYPE);
				
				System.out.println(INDENT + "--- Base type ---");
				printType(baseTypeSpec);
				
				if (baseTypeSpec.getIdentifier() == null) {
					printTypeDetail(baseTypeSpec, recordTypes);
				}
				
				System.out.println(INDENT + "Range = ");
				System.out.println(toString(minValue) + ".." + toString(maxValue));
				
				break;
				
			}
			
			case ARRAY: {
				TypeSpec indexType = (TypeSpec) type.getAttribute(ARRAY_INDEX_TYPE);
				TypeSpec elementType = (TypeSpec) type.getAttribute(ARRAY_ELEMENT_TYPE);
				int count = (Integer) type.getAttribute(ARRAY_ELEMENT_COUNT);
				
				System.out.println(INDENT + "--- INDEX TYPE ---");
				printType(indexType);
				
				if (indexType.getIdentifier() == null) {
					printTypeDetail(indexType, recordTypes);
			
				}
				
				System.out.println(INDENT + "--- ELEMENT TYPE ---");
				printType(elementType);
				System.out.println(INDENT.toString() + count + " elements");
				
				if (elementType.getIdentifier() == null) {
					printTypeDetail(elementType, recordTypes);
				}
				
				break;
			}
			
			case RECORD: {
				recordTypes.add(type);
				break;
			}
		}
	}
	
	private void printRecords(ArrayList<TypeSpec> recordTypes) {
		
		for (TypeSpec recordType : recordTypes) {
			
			SymTabEntry recordId = recordType.getIdentifier();
			String name = recordId != null ? recordId.getName() : "<unnamed>";
			
			System.out.println("\n--- RECORD " + name + " ---");
			printColumnHeadings();
			
			SymTab symTab = (SymTab) recordType.getAttribute(RECORD_SYMTAB);
			ArrayList<TypeSpec> newRecordTypes = new ArrayList<TypeSpec>();
			printSymTab(symTab, newRecordTypes);
			
			if (newRecordTypes.size() > 0) {
				printRecords(newRecordTypes);
			}
		}
	}
	
	private String toString(Object value) {
		return value instanceof String ? "'" + (String) value + "'"
									  : value.toString();
	}


}
