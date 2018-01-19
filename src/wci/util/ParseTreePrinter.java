package wci.util;

import static wci.intermediate.symtabimpl.SymTabKeyImpl.ROUTINE_ICODE;
import static wci.intermediate.symtabimpl.SymTabKeyImpl.ROUTINE_ROUTINES;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import wci.intermediate.Definition;
import wci.intermediate.ICode;
import wci.intermediate.ICodeKey;
import wci.intermediate.ICodeNode;
import wci.intermediate.SymTabEntry;
import wci.intermediate.SymTabStack;
import wci.intermediate.icodeimpl.ICodeNodeImpl;

/**
 * <h1>ParseTreePrinter</h1>
 * 
 * <p>Print a parse tree.</p>
 */
public class ParseTreePrinter 
{
	private static final int INDENT_WIDTH = 4;
	private static final int LINE_WIDTH = 50;
	
	private PrintStream printStream;				
	private int length;					
	private String indent;				
	private String indentation;			
	private StringBuilder line;			


	public ParseTreePrinter(PrintStream printStream) 
	{
		this.printStream = printStream;
		this.length = 0;
		this.indentation = "";
		this.line = new StringBuilder();
		
		//	The indent is INDENT_WIDTH spaces
		this.indent = "";
		for (int i = 0; i < INDENT_WIDTH; i++)
		{
			this.indent += " ";
		}
	}

	public void print(SymTabStack symTabStack)
	{
		printStream.println("\n===== INTERMEDIATE CODE =====\n");
		
		SymTabEntry programId = symTabStack.getProgramId();
		printRoutine(programId);
	}
	
	public void printRoutine(SymTabEntry routineId) {
		Definition definition = routineId.getDefinition();
		System.out.println("\n*** " + definition.toString() + " " + routineId.getName() + "***");
		ICode iCode = (ICode) routineId.getAttribute(ROUTINE_ICODE);
		if (iCode.getRoot() != null) {
			printNode((ICodeNodeImpl) iCode.getRoot());
			
		}
		
		ArrayList<SymTabEntry> routineIds = (ArrayList<SymTabEntry>) routineId.getAttribute(ROUTINE_ROUTINES);
		if (routineIds != null) {
			for (SymTabEntry rtnId : routineIds) {
				printRoutine(rtnId);
			}
		}
	}
	
	
	public void printNode(ICodeNodeImpl node)
	{
		append(indentation); append("<" + node.toString());
		
		printAttributes(node);
		printTypeSpec(node);
		
		ArrayList<ICodeNode> childNodes = node.getChildren();
		if ((childNodes != null) && (childNodes.size() > 0))
		{
			append(">");
			printLine();
			
			printChildNodes(childNodes);
			append(indentation); append("</" + node.toString() + ">");
		}
	
		else 
		{
			append(" "); append("/>");
		}
		
		printLine();
		
	}
	
	public void printAttributes(ICodeNodeImpl node)
	{
		String saveIndentation = indentation;
		indentation += indent;
		Set<Map.Entry<ICodeKey, Object>> attributes = node.entrySet();
		Iterator<Map.Entry<ICodeKey, Object>> it = attributes.iterator();
		
		//	Iterate to print each attribute.
		while (it.hasNext())
		{
			Map.Entry<ICodeKey, Object> attribute = it.next();
			printAttributes(attribute.getKey().toString(), attribute.getValue());
		}
		
		indentation = saveIndentation;
	}
	
	public void printAttributes(String keyString, Object value)
	{
		//	If the value is a symbol table entry, use the identifier's name
		//	Else just use the value string.
		boolean isSymTabEntry = value instanceof SymTabEntry;
		String valueString = isSymTabEntry ? ((SymTabEntry) value).getName() : value.toString();
		
		String text = keyString.toLowerCase() + "=\"" + valueString + "\"";
		append(" "); append(text);
		
		//	Include as identifier's nesting level.
		if (isSymTabEntry)
		{
			int level = ((SymTabEntry) value).getSymTab().getNestingLevel();
			printAttributes("LEVEL", level);
		}
	}
	
	public void printChildNodes(ArrayList<ICodeNode> childNodes)
	{
		String saveIndentation = indentation;
		indentation += indent;
		
		for (ICodeNode child : childNodes)
		{
			printNode((ICodeNodeImpl) child);
		}
		
		indentation = saveIndentation;
		
	}
	
	public void printTypeSpec(ICodeNodeImpl node)
	{
		
	}
	

	private void append(String text)
	{
		int textLength = text.length();
		boolean lineBreak = false;
		
		//	Wrap lines that are too long
		if (length + textLength > LINE_WIDTH)
		{
			printLine();
			line.append(indentation);
			length = indentation.length();
			lineBreak = true;
		}
		
		//	Append the text.
		if (!(lineBreak && text.equals(" ")))
		{
			line.append(text);
			length += textLength;
		}
	}
	
	private void printLine()
	{
		if (length > 0)
		{
			printStream.println(line);
			line.setLength(0);
			length = 0;
		}
	}
}
