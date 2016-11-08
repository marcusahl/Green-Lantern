package wci.frontend.pascal;

import wci.frontend.*;
import wci.frontend.pascal.tokens.*;

import static wci.frontend.pascal.PascalErrorCode.*;
import static wci.frontend.Source.EOF;


/**
 * <h1>PascalScanner</h1>
 * 
 *<p>The Pascal scanner</p>
 */
public class PascalScanner extends Scanner {

	/**
	 * Constructor
	 * @param source the source to be used with this scanner.
	 */
	public PascalScanner(Source source) 
	{
		super(source);
	}

	/**
	 * Extract and return the next Pascal token from the source.
	 * @return the next token
	 * throws Exception if an error occurred
	 */
	protected Token extractToken() 
		throws Exception 
	{
		skipWhiteSpace();
		
		Token token;
		char currentChar = currentChar();
		
		// Construct the next token. The current character determines the
		/// token type.
		if (currentChar == EOF) 
		{
			token = new EofToken(source);
		}
		
		else if (Character.isLetter(currentChar))
		{
			token = new PascalWordToken(source);
		}
		
		else if (Character.isDigit(currentChar))
		{
			token = new PascalNumberToken(source);
		}
		
		else if (currentChar == '\'')
		{
			token = new PascalStringToken(source);
		}

		else if (PascalTokenType.SPECIAL_SYMBOLS.containsKey(Character.toString(currentChar)))
		{
			token = new PascalSpecialSymbolToken(source);
		}
		
		else
		{
			token = new PascalErrorToken(source, INVALID_CHARACTER, Character.toString(currentChar));
			
			nextChar(); // consumes character
		}
		
		return token;
		
	}
	
	/**
	 * Skips whitespace characters by consuming them. A comment is whitespace.
	 * @throws Exception
	 */
	protected void skipWhiteSpace() 
		throws Exception
	{
		char currentChar = currentChar();
		
		while (Character.isWhitespace(currentChar) || currentChar == '{')
		{
			// Start of a comment?
			if (currentChar == '{')
			{
				do
				{
					currentChar = nextChar();	// consumes the character
				} while ((currentChar != '}') && (currentChar != EOF));
				
				//Found a closing }?
				if (currentChar == '}')
				{
					currentChar = nextChar(); 		// consume whitespace character
				}
			}
			
			
			// Not a comment
			else
			{
				currentChar = nextChar(); 			// consume whitespace character
			}

		}

	}

}
