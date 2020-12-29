package wci.backend.interpreter.debuggerimpl;

import wci.backend.Backend;
import wci.backend.interpreter.Debugger;
import wci.backend.interpreter.RuntimeStack;
import wci.intermediate.Definition;
import wci.intermediate.SymTabEntry;
import wci.message.Message;

import javax.naming.Name;
import java.util.ArrayList;

import static wci.ide.IDEControl.*;

public class GUIDebugger extends Debugger {

    private CommandProcessor commandProcessor;

    public GUIDebugger(Backend backend, RuntimeStack runtimeStack) {
        super(backend, runtimeStack);
        commandProcessor = new CommandProcessor(this);
    }

    @Override
    public void processMessage(Message message) {
        commandProcessor.processMessage(message);
    }

    @Override
    public void promptForCommand() {}

    @Override
    public boolean parseCommand() {
        return commandProcessor.parseCommand();
    }

    @Override
    public void atStatement(Integer lineNumber) {
        System.out.println(DEBUGGER_AT_TAG + lineNumber);
    }

    @Override
    public void atBreakpoint(Integer lineNumber) {
        System.out.println(DEBUGGER_BREAK_TAG + lineNumber);
    }

    @Override
    public void atWatchpointValue(Integer lineNumber, String name, Object value) {}

    @Override
    public void atWatchpointAssignment(Integer lineNumber, String name, Object value) {}

    @Override
    public void callRoutine(Integer lineNumber, String routineName) {}

    @Override
    public void returnRoutine(Integer lineNumber, String routineName) {}

    @Override
    public void displayValue(String valueString) {
        System.out.println(valueString);
    }

    @Override
    public void displayCallStack(ArrayList stack) {
        // Call stack header
        System.out.println(DEBUGGER_ROUTINE_TAG + -1);

        for (Object item : stack) {

            //] Name of a procedure of function
            if (item instanceof SymTabEntry) {
                SymTabEntry routineId = (SymTabEntry) item;
                String routineName = routineId.getName();
                int level = routineId.getSymTab().getNestingLevel();
                Definition definition = routineId.getDefinition();

                System.out.println(DEBUGGER_ROUTINE_TAG + ":" + definition.getText().toUpperCase() + " " + routineName);
            }
            // Variable name-value pair
            else if (item instanceof NameValuePair) {
                NameValuePair pair = (NameValuePair) item;
                System.out.println(DEBUGGER_VARIABLE_TAG + pair.getVariableName() + ":");
                displayValue(pair.getValueString());
            }
        }
        // Call stack footer
        System.out.println(DEBUGGER_ROUTINE_TAG + -2);
    }

    @Override
    public void quit() {
        System.out.println("!INTERPRETER: Program terminated.");
        System.exit(-1);

    }

    @Override
    public void commandError(String errorMessage) {
        runtimeError(errorMessage, 0);
    }

    @Override
    public void runtimeError(String errorMessage, Integer lineNumber) {
        System.out.println("*** RUNTIME ERROR");
        if (lineNumber != null) {
            System.out.println(" AT LINE " + String.format("%03d", lineNumber));
        }
        System.out.println(": " + errorMessage);
    }
}
