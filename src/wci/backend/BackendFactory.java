package wci.backend;

import wci.backend.compiler.CodeGenerator;
import wci.backend.interpreter.Debugger;
import wci.backend.interpreter.Executor;
import wci.backend.interpreter.RuntimeStack;
import wci.backend.interpreter.debuggerimpl.CommandLineDebugger;
import wci.backend.interpreter.debuggerimpl.DebuggerType;
import wci.backend.interpreter.debuggerimpl.GUIDebugger;
import wci.intermediate.TypeSpec;
import wci.intermediate.symtabimpl.Predefined;

public class BackendFactory {

	public static Backend createBackend(String operation, String inputPath) throws Exception {
		if(operation.equalsIgnoreCase("compile")) {
			return new CodeGenerator();
		} else if (operation.equalsIgnoreCase("execute")) {
			return new Executor(inputPath);
		} else {
			throw new Exception("Backend factory: Invalid operation '" + operation +"'");
		}
	}

	public static Debugger createDebugger(DebuggerType type, Backend backend, RuntimeStack runtimeStack) {
		switch (type) {
			case COMMAND_LINE: return new CommandLineDebugger(backend, runtimeStack);
			case GUI: return new GUIDebugger(backend, runtimeStack);
			default: return null; // Cant be reached
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
