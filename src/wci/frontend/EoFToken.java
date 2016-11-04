package wci.frontend;

public class EoFToken extends Token {

	/**
	 * Constructor
	 * @param source the source from where to fetch the token's characters.
	 * @throws Exception if an error occurred.
	 */
	public EoFToken(Source source) 
		throws Exception 
	{
		super(source);
	}
	/**
	 * Do nothing. Do not consume any source characters
	 * @param source the source from where to fetch the token's characters.
	 * @throws Exception if an error occurred.
	 */
	protected void extract(Source source)
		throws Exception
	{
		
	}
}
