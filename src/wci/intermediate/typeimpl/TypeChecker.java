package wci.intermediate.typeimpl;

import static wci.intermediate.typeimpl.TypeFormImpl.*;

import wci.intermediate.TypeForm;
import wci.intermediate.TypeSpec;
import wci.intermediate.symtabimpl.Predefined;

public class TypeChecker {
	
	public static boolean areAssignmentCompatible(TypeSpec targetType, TypeSpec valueType) {
		
		if((targetType == null) || (valueType == null)) {
			return false;
		}
		
		targetType = targetType.baseType();
		valueType = valueType.baseType();
		
		return ((targetType == valueType) || 
				(isReal(targetType) && isInteger(valueType))) ||
				(targetType.isPascalString() && valueType.isPascalString());
		
	}
	
	public static boolean areComparisonCompatible(TypeSpec type1, TypeSpec type2) {
		
		if (type1 == null || type2 == null) {
			return false;
		}
			type1 = type1.baseType();
			type2 = type2.baseType();
			TypeForm form = type1.getForm();

			// Two identical scalar or enum types
		if ((type1 == type2) && ((form == SCALAR || (form == ENUMERATION)))) {
			return true;
		}
		// One is an integer and one is real
		else if (areBothNumbersAndAtLeastOneReal(type1, type2)) {
			return true;
		}
		// Two strings
		return type1.isPascalString() && type2.isPascalString();
	}
	
	public static boolean isInteger(TypeSpec type) {
		return (type != null) && (type.baseType() == Predefined.integerType);
	}
	
	public static boolean isReal(TypeSpec type) {
		return (type != null) && (type.baseType() == Predefined.realType);
	}
	
	public static boolean areBothInteger(TypeSpec type1, TypeSpec type2) {
		return isInteger(type1) && isInteger(type2);
	}
		
	public static boolean isIntegerOrReal (TypeSpec type) {
		return isInteger(type) || isReal(type);
	}
	
	public static boolean areBothNumbersAndAtLeastOneReal (TypeSpec type1, TypeSpec type2) {
		return ((isReal(type1) && isInteger(type2)) || 
			   (isInteger(type1) && isReal(type2)) ||
			   (isReal(type1) && isReal(type2)));
	}
	
	public static boolean isBoolean(TypeSpec type) {
		return (type != null) && (type.baseType() == Predefined.booleanType);
	}
	
	public static boolean areBothBoolean(TypeSpec type1, TypeSpec type2) {
		return isBoolean(type1) && isBoolean(type2);
	}
	
	public static boolean isChar(TypeSpec type) {
		return (type != null) && (type.baseType() == Predefined.charType);
	}

}
