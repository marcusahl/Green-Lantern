package wci.backend;

import wci.backend.compiler.CodeGenerator;
import wci.backend.interpreter.Executor;
import wci.intermediate.TypeSpec;
import wci.intermediate.symtabimpl.Predefined;

public class BackendFactory {

	public static Backend createBackend(String operation) throws Exception {
		if(operation.equalsIgnoreCase("compile")) {
			return new CodeGenerator();
		} else if (operation.equalsIgnoreCase("execute")) {
			return new Executor();
		} else {
			throw new Exception("Backend factory: Invalid operation '" + operation +"'");
		}
	}

	public static Object defaultValue(TypeSpec type) {
		type = type.baseType();
		if (type == Predefined.integerType) {
			return Integer.valueOf(0);
		} else if (type == Predefined.realType) {
			return Float.valueOf(0.0f);
		} else if (type == Predefined.booleanType) {
			return Boolean.valueOf(false);
		} else if (type == Predefined.charType) {
			return Character.valueOf('#');
		} else return String.valueOf("#");
	}

}
