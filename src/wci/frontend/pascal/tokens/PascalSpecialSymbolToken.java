package wci.frontend.pascal.tokens;

import wci.frontend.*;
import wci.frontend.pascal.*;

import static wci.frontend.pascal.PascalTokenType.*;
import static wci.frontend.pascal.PascalErrorCode.*;


public class PascalSpecialSymbolToken extends PascalToken {

	/**
	 * Constructor
	 * @param source the source to fetch subsequent characters from.
	 * @throws Exception if an error occurred.
	 */
	public PascalSpecialSymbolToken(Source source) 
		throws Exception 
	{
		super(source);
	}
	
	/**
	 * Extract a Pascal special symbol token from the source.
	 * @throws Exception if an error occurred.
	 */
	protected void extract()
		throws Exception
	{
		char currentChar = currentChar();
		
		text = Character.toString(currentChar);
		type = null;
		
		switch (currentChar)
		{
			// Single-character special symbols.
			case '+': case '-': case '*': case '/': case ',':
			case ';': case '\'': case '=': case '(': case ')':
			case '{': case '}': case '[': case ']': case '^':
			{
				nextChar();						// consumes character
				break;
			}
			
			// : or :=
			case ':': 
			{
				currentChar = nextChar();		// consumes ':'
				
				if (currentChar == '=')
				{
					text += currentChar;
					nextChar(); 				// consumes '='
				}
				
				break;
			}
			
			// < or <= or <>
			case '<': 
			{
				currentChar = nextChar(); 		// consumes '<'
				
				if ((currentChar == '=') || currentChar == '>')
				{
					text += currentChar;
					nextChar(); 				// consumes '='/'>'
				}
				
				break;
			}
			
			// > or >= 
			case '>': 
			{
				currentChar = nextChar(); 		// consumes '>'
				
				if (currentChar == '=')
				{
					text += currentChar;
					nextChar(); 				// consumes '='
				}
				
				break;
			}
			
			
			// . or ..
			case '.':
			{
				currentChar = nextChar();		// consumes '.'
				
				if (currentChar == '.')
				{
					text += currentChar;
					nextChar();					// consumes '.'
				}
				
				break;
			}
			default:
			{
				nextChar();						// consumes bad character
				type = ERROR;
				value = INVALID_CHARACTER;
			}
		
		}
		
		// Set the type if there was no error
		if (type == null)
		{
			type = SPECIAL_SYMBOLS.get(text);
		}
		
	}

}
