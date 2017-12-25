package wci.frontend.pascal;

import wci.frontend.*;
import wci.frontend.pascal.tokens.*;

import static wci.frontend.pascal.PascalErrorCode.*;
import static wci.frontend.Source.EOF;

public class PascalScanner extends Scanner {

	public PascalScanner(Source source) 
	{
		super(source);
	}

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
