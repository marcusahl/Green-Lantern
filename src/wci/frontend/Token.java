package wci.frontend;

/**
 * <h1>Token</h1>
 * 
 * <p>The framework class that represents a token
 * returned by the scanner </p>
 */
public class Token 
{
	protected TokenType type; //language-specific token type
	protected String text; //token text
	protected Object value; //token value
	protected Source source;
	protected int lineNum;
	protected int position;
	
	/**
	 * Constructor
	 * @param source the source from where to fetch the token's characters.
	 * @throws Exception if an error occurred.
	 */
	public Token(Source source)
		throws Exception
	{
		this.source = source;
		this.lineNum = source.getLineNum();
		this.position = source.getPosition();
		
		extract();
	}
	
	/**
	 * Default method to extract only one-character tokens from the source.
	 * Subclasses can override this method to construct language-
	 * specific tokens. After extracting the token, the current
	 * source line position will be one beyond the last token character.
	 * @throws Exception if an error occurred.
	 */
	protected void extract()
		throws Exception
	{
		text = Character.toString(currentChar());
		value = null;
		
		nextChar();		// consumes current character
		
	}
	
	/**
	 * Call the source's currentChar() method.
	 * @return the current character from the source.
	 * @throws Exception if an error occurred.
	 */
	protected char currentChar()
		throws Exception
	{
		return source.currentChar();
	}
	
	/**
	 * Call the source's nextChar() method.
	 * @return the next character from the source.
	 * @throws Exception if an error occurred.
	 */
	protected char nextChar()
		throws Exception
	{
		return source.nextChar();
	}
	
	/**
	 * Call the source's peekChar() method.
	 * @return the next character from the source without moving forward.
	 * @throws Exception if an error occurred.
	 */
	protected char peekChar()
		throws Exception
	{
		return source.peekChar();
	}

}
