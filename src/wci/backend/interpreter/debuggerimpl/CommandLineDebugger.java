package wci.backend.interpreter.debuggerimpl;

import wci.backend.Backend;
import wci.backend.interpreter.Debugger;
import wci.backend.interpreter.RuntimeStack;
import wci.intermediate.Definition;
import wci.intermediate.SymTabEntry;
import wci.message.Message;

import java.util.ArrayList;

public class CommandLineDebugger extends Debugger {

    private CommandProcessor commandProcessor;

    public CommandLineDebugger(Backend backend, RuntimeStack runtimeStack) {
        super(backend, runtimeStack);
        commandProcessor = new CommandProcessor(this);
    }

    @Override
    public void processMessage(Message message) {
        commandProcessor.processMessage(message);
    }

    @Override
    public void promptForCommand() {
        System.out.println(">>> Command? ");
    }

    @Override
    public boolean parseCommand() {
        return commandProcessor.parseCommand();
    }

    @Override
    public void atStatement(Integer lineNumber) {
        System.out.println("\n>>> At line " + lineNumber);
    }

    @Override
    public void atBreakpoint(Integer lineNumber) {
        System.out.println("\n>>> Breakpoint at line " + lineNumber);
    }

    @Override
    public void atWatchpointValue(Integer lineNumber, String name, Object value) {
        System.out.println("\n>>> At line " + lineNumber + ": " + name + ": " + value.toString());
    }

    @Override
    public void atWatchpointAssignment(Integer lineNumber, String name, Object value) {
        System.out.println("\n>>> At line " + lineNumber + ": " + name + " := " + value.toString());
    }

    @Override
    public void callRoutine(Integer lineNumber, String routineName) {
        // TODO
    }

    @Override
    public void returnRoutine(Integer lineNumber, String routineName) {
        // TODO
    }

    @Override
    public void displayValue(String valueString) {
        System.out.println(valueString);
    }

    @Override
    public void displayCallStack(ArrayList stack) {
        for (Object item : stack) {
            // Name of a procedure or function
            if (item instanceof SymTabEntry) {
                SymTabEntry routineId = (SymTabEntry) item;
                String routineName = routineId.getName();
                int level = routineId.getSymTab().getNestingLevel();
                Definition definition = routineId.getDefinition();

                System.out.println(String.valueOf(level) + ": " + definition.getText().toUpperCase() + " " + routineName);

            }
            // Variable name-value pair
            else if (item instanceof NameValuePair) {
                NameValuePair pair = (NameValuePair) item;
                System.out.println(" " + pair.getVariableName() + ": ");
                displayValue(pair.getValueString());
            }
        }
    }

    @Override
    public void quit() {
        System.out.println("Program terminated.");
        System.exit(-1);
    }

    @Override
    public void commandError(String errorMessage) {
        System.out.println("!!! ERROR: " + errorMessage);
    }

    @Override
    public void runtimeError(String errorMessage, Integer lineNumber) {
        System.out.print("!!! RUNTIME ERROR");
        if (lineNumber != null) {
            System.out.print(" at line " + String.format("%03d", lineNumber));
        }
        System.out.println(": " + errorMessage);
    }
}
