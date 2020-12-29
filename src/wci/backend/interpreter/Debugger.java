package wci.backend.interpreter;

import wci.backend.Backend;
import wci.frontend.Scanner;
import wci.frontend.Source;
import wci.frontend.Token;
import wci.frontend.TokenType;
import wci.frontend.pascal.PascalScanner;
import wci.frontend.pascal.PascalTokenType;
import wci.message.Message;
import wci.message.MessageListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;

import static wci.frontend.pascal.PascalTokenType.*;

public abstract class Debugger {
    private HashSet<Integer> breakpoints;
    private HashSet<String> watchpoints;
    private RuntimeStack runtimeStack;
    private Scanner commandInput;

    public Debugger(Backend backend, RuntimeStack rs) {
        runtimeStack = rs;
        breakpoints = new HashSet<>();
        watchpoints = new HashSet<>();
        backend.addMessageListener(new BackendMessageListener());

        try {
            commandInput = new PascalScanner(new Source(new BufferedReader(new InputStreamReader(System.in))));
        } catch (IOException swallowed) {}

    }

    private class BackendMessageListener implements MessageListener {
        public void messageReceived(Message message) { processMessage(message); }
    }

    public RuntimeStack getRuntimeStack() { return runtimeStack; }

    public void readCommands() {
        do {
            promptForCommand();
        } while(parseCommand());
    }

    public Token currentToken() throws Exception {
        return commandInput.currentToken();
    }

    public Token nextToken() throws Exception {
        return commandInput.nextToken();
    }

    public String getWord(String errorMessage) throws Exception {
        Token token = currentToken();
        TokenType type = token.getType();

        if (type == IDENTIFIER) {
            String word = token.getText().toLowerCase();
            nextToken();
            return word;
        } else {
            throw new Exception((errorMessage));
        }
    }

    public Integer getInteger(String errorMessage) throws Exception {
        Token token = currentToken();
        TokenType type = token.getType();

        if (type == INTEGER) {
            Integer value = (Integer) (token.getValue());
            nextToken();
            return value;
        } else {
            throw new Exception(errorMessage);
        }
    }

    public Object getValue(String errorMessage) throws Exception {
        Token token = currentToken();
        TokenType type = token.getType();
        boolean sign = false;
        boolean minus = false;

        if (type == MINUS | type == PLUS) {
            sign = true;
            minus = type == MINUS;
            token = nextToken();
            type = token.getType();
        }

        switch ((PascalTokenType) type) {

            case INTEGER: {
                Integer value = (Integer) token.getValue();
                nextToken();
                return minus ? -value : value;
            }

            case REAL: {
                Float value = (Float) token.getValue();
                nextToken();
                return minus ? -value : value;
            }

            case STRING: {
                if (sign) {
                    throw new Exception(errorMessage);
                } else {
                    String value = (String) token.getValue();
                    nextToken();
                    return value.charAt(0);
                }
            }

            case IDENTIFIER: {
                if (sign) {
                    throw new Exception(errorMessage);
                } else {
                    String name = token.getText();
                    nextToken();

                    if (name.equalsIgnoreCase("true")) {
                        return true;
                    } else if (name.equalsIgnoreCase("false")) {
                        return false;
                    } else {
                        throw new Exception(errorMessage);
                    }
                }
            }

            default: {
                throw new Exception(errorMessage);
            }
        }
    }

    public void skipToNextCommand() throws Exception {
        commandInput.skipToNextLine();
    }
    public void setBreakpoint(Integer lineNumber) {
        breakpoints.add(lineNumber);
    }
    public void unsetBreakpoint(Integer lineNumber) {
        breakpoints.remove(lineNumber);
    }
    public boolean isBreakpoint(Integer lineNumber) {
        return breakpoints.contains(lineNumber);
    }
    public void setWatchpoint(String name) {
        watchpoints.add(name);
    }
    public void unsetWatchpoint(String name) {
        watchpoints.remove(name);
    }
    public boolean isWatchpoint(String name) {
        return watchpoints.contains(name);
    }

    // Abstract methods
    public abstract void processMessage(Message message);
    public abstract void promptForCommand();
    public abstract boolean parseCommand();
    public abstract void atStatement(Integer lineNumber);
    public abstract void atBreakpoint(Integer lineNumber);
    public abstract void atWatchpointValue(Integer lineNumber, String name, Object value);
    public abstract void atWatchpointAssignment(Integer lineNumber, String name, Object value);
    public abstract void callRoutine(Integer lineNumber, String routineName);
    public abstract void returnRoutine(Integer lineNumber, String routineName);
    public abstract void displayValue(String valueString);
    public abstract void displayCallStack(ArrayList stack);
    public abstract void quit();
    public abstract void commandError(String errorMessage);
    public abstract void runtimeError(String errorMessage, Integer lineNumber);

}

