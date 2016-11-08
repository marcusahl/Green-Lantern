package wci.frontend.pascal.tokens;

import wci.frontend.*;
import wci.frontend.pascal.*;

import static wci.frontend.pascal.PascalTokenType.*;
import static wci.frontend.Source.EOF;
import static wci.frontend.pascal.PascalErrorCode.*;

public class PascalStringToken extends PascalToken {

	/**
	 * Constructor.
	 * @param source the source to fetch subsequent characters.
	 * @throws Exception if an error occurred.
	 */
	public PascalStringToken(Source source) 
		throws Exception 
	{
		super(source);
	}
	
	/**
	 * Extract a Pascal string token from the source
	 * @throws Exception if an error occurred
	 */
	protected void extract()
		throws Exception
	{
		StringBuilder textBuffer = new StringBuilder();
		StringBuilder valueBuffer = new StringBuilder();
		
		char currentChar = nextChar();		// consumes the initial quote
		textBuffer.append("\'");
		
		// Get string characters
		do
		{
			// Replace any whitespace character with a blank
			if (Character.isWhitespace(currentChar))
			{
				currentChar = ' ';
			}
			
			if ((currentChar != '\'') && (currentChar != EOF))
			{
				textBuffer.append(currentChar);
				valueBuffer.append(currentChar);
				currentChar = nextChar(); 		// consumes the character
			}
			
			//Each pair of adjacent quotes represent a single quote
			if (currentChar == '\'')
			{
				while ((currentChar == '\'') && (peekChar() == '\''))
				{
					textBuffer.append("''");
					valueBuffer.append(currentChar); 		// append single quote
					currentChar = nextChar();				// consumes pair of quotes
					currentChar = nextChar();
				}
			}
		} while ((currentChar != '\'') && (currentChar != EOF));
		
		if (currentChar == '\'')
		{
			nextChar();			// consume final quote
			textBuffer.append('\'');
			
			type = STRING;
			value = valueBuffer.toString();
		}
		
		else 
		{
			type = ERROR;
			value = UNEXPECTED_EOF;
		}
		
		text = textBuffer.toString();
		
	}

}
