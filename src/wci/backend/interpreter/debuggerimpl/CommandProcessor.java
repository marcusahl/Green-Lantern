package wci.backend.interpreter.debuggerimpl;

import wci.backend.interpreter.*;
import wci.intermediate.SymTab;
import wci.intermediate.SymTabEntry;
import wci.message.Message;
import wci.message.MessageType;

import java.util.ArrayList;
import java.util.Locale;

import static wci.frontend.pascal.PascalTokenType.SEMICOLON;
import static wci.intermediate.symtabimpl.SymTabKeyImpl.ROUTINE_SYMTAB;

public class CommandProcessor {

    private Debugger debugger;
    private boolean is_single_stepping;

    public CommandProcessor(Debugger debugger) { this.debugger = debugger; }

    protected void processMessage(Message message) {
        MessageType type = message.getType();

        switch (type) {

            case SOURCE_LINE: {
                int lineNumber = (Integer) message.getBody();

                if (is_single_stepping) {
                    debugger.atStatement(lineNumber);
                    debugger.readCommands();
                }
                else if(debugger.isBreakpoint(lineNumber)) {
                    debugger.atBreakpoint(lineNumber);
                    debugger.readCommands();
                }
                break;
            }

            case FETCH: {
                Object body[] = (Object[]) message.getBody();
                String variableName = (String.valueOf(body[1]).toLowerCase());

                if (debugger.isWatchpoint(variableName)) {
                    int lineNumber = (Integer) body[0];
                    Object value = body[2];
                    debugger.atWatchpointValue(lineNumber, variableName, value);
                }
                break;
            }

            case ASSIGN: {
                Object body[] = (Object[]) message.getBody();
                String variableName = (String.valueOf(body[1]).toLowerCase());

                if (debugger.isWatchpoint(variableName)) {
                    int lineNumber = (Integer) body[0];
                    Object value = body[2];
                    debugger.atWatchpointAssignment(lineNumber, variableName, value);
                }
                break;
            }

            case CALL: {
                Object[] body = (Object[]) message.getBody();
                int lineNumber = (Integer) body[0];
                String routineName = String.valueOf(body[1]);
                debugger.callRoutine(lineNumber, routineName);
                break;
            }

            case RETURN: {
                Object[] body = (Object[]) message.getBody();
                int lineNumber = (Integer) body[0];
                String routineName = String.valueOf(body[1]);
                debugger.returnRoutine(lineNumber, routineName);
                break;
            }

            case RUNTIME_ERROR: {
                Object[] body = (Object[]) message.getBody();
                String errorMessage = String.valueOf(body[0]);
                int lineNumber = (Integer) body[1];
                debugger.runtimeError(errorMessage, lineNumber);
                break;
            }
        }
    }

    public boolean parseCommand() {
        boolean anotherCommand = true;

        try {
            debugger.nextToken();
            String command = debugger.getWord("Command expected.");
            anotherCommand = executeCommand(command);
        }
        catch (Exception e) {
            debugger.commandError(e.getMessage());
        }

        // Skip to the next command
        try {
            debugger.skipToNextCommand();
        }
        catch (Exception e) {
            debugger.commandError(e.getMessage());
        }
        return anotherCommand;
    }

    private boolean executeCommand(String command) throws Exception {
        is_single_stepping = false;

        if(command.equals("step")) {
            is_single_stepping = true;
            checkForSemicolon();
            return false;
        }
        if(command.equals("break")) {
            Integer lineNumber = debugger.getInteger("Line number expected.");
            checkForSemicolon();
            debugger.setBreakpoint(lineNumber);
            return true;
        }
        if (command.equals("unbreak")) {
            Integer lineNumber = debugger.getInteger("Line number expected.");
            checkForSemicolon();
            debugger.unsetBreakpoint(lineNumber);
            return true;
        }
        if (command.equals("watch")) {
            String name = debugger.getWord("Variable name expected.");
            checkForSemicolon();
            debugger.setWatchpoint(name);
            return true;
        }
        if (command.equals("unwatch")) {
            String name = debugger.getWord("Variable name expected.");
            checkForSemicolon();
            debugger.unsetWatchpoint(name);
            return true;
        }
        if (command.equals("stack")) {
            checkForSemicolon();
            stack();
            return true;
        }
        if (command.equals("show")) {
            show();
            return true;
        }
        if (command.equals("assign")) {
            assign();
            return true;
        }
        if (command.equals("go")) {
            checkForSemicolon();
            return true;
        }
        if (command.equals("quit")) {
            checkForSemicolon();
            debugger.quit();
        }

        throw new Exception("Invalid command: '" + command + "'.");
    }

    private void checkForSemicolon() throws Exception {
        if (debugger.currentToken().getType() != SEMICOLON) {
            throw new Exception("Invalid command syntax.");
        }
    }

    private void stack() {
        ArrayList callStack = new ArrayList();
        RuntimeStack runtimeStack = debugger.getRuntimeStack();
        ArrayList<ActivationRecord> records = runtimeStack.records();
        for (int i = records.size() -1; i >=0; --i) {
            ActivationRecord ar = records.get(i);
            SymTabEntry routineId = ar.getRoutineId();
            callStack.add(routineId);

            for(String name : ar.getAllNames()) {
                Object value = ar.getCell(name).getValue();
                callStack.add(new NameValuePair(name, value));
            }
        }
        debugger.displayCallStack(callStack);
    }

    private void show() throws Exception {
        CellTypePair pair = createCellTypePair();
        Cell cell = pair.getCell();
        checkForSemicolon();
        debugger.displayValue(NameValuePair.valueString(cell.getValue()));
    }

    private void assign() throws Exception {
        CellTypePair pair = createCellTypePair();
        Object newValue = debugger.getValue("Invalid value.");
        checkForSemicolon();
        pair.setValue(newValue);
    }

    private CellTypePair createCellTypePair() throws Exception {
        RuntimeStack runtimeStack = debugger.getRuntimeStack();
        int currentLevel = runtimeStack.currentNestingLevel();
        ActivationRecord ar = null;
        Cell cell = null;

        String variableName = debugger.getWord("Variable name expected.");

        // Find the variable's cell in the callstack
        for (int level = currentLevel; (cell == null && level > 0); --level) {
            ar = runtimeStack.getTopMost(level);
            cell = ar.getCell(variableName);
        }

        if (cell == null) {
            throw new Exception("Undeclared variable name '" + variableName + "'.");
        }

        // VAR parameter
        if (cell.getValue() instanceof Cell) {
            cell = (Cell) cell.getValue();
        }

        SymTab symTab = (SymTab) ar.getRoutineId().getAttribute(ROUTINE_SYMTAB);
        SymTabEntry id = symTab.lookup(variableName);

        return new CellTypePair(id.getTypeSpec(), cell, debugger);
    }
}
