package wci.frontend.pascal.tokens;

import wci.frontend.*;
import wci.frontend.pascal.*;

import static wci.frontend.pascal.PascalTokenType.*;

public class PascalWordToken extends PascalToken {

	/**
	 * Constructor.
	 * @param source the source to fetch subsequent characters.
	 * @throws Exception if an error occurred.
	 */
	public PascalWordToken(Source source) 
			throws Exception 
	{
		super(source);
	}
	
	/**
	 * Extracts a Pascal word token from the source
	 * @throws Exception if an error occurred
	 */
	protected void extract()
		throws Exception
	{
		StringBuilder textBuffer = new StringBuilder();
		char currentChar = currentChar();
		
		// Get the word characters (letter or digit). The scanner has 
		// already determined that the first character is a letter.
		while (Character.isLetterOrDigit(currentChar)) 
		{
			textBuffer.append(currentChar);
			currentChar = nextChar();	// consumes the character
		}
		
		text = textBuffer.toString();
		
		// Is it a reserved word or an identifier?
		type = (RESERVED_WORDS.contains(text.toLowerCase()))
				? PascalTokenType.valueOf(text.toUpperCase()) 		// Reserved word
				: IDENTIFIER;										// identifier
	}

}
