package wci.message;

/**
 * <h1>Message</h1>
 * 
 *<p>Message format. </p>
 */


public class Message {

	private MessageType type;
	private Object body;

	/**
	 * Constructor.
	 * @param type the message type.
	 * @param body the message body.
	 */
	public Message(MessageType type, Object body) 
	{
		this.type = type;
		this.body = body;
	}
	
	/**
	 * Returns the message type.
	 * @return type the message type.
	 */
	public MessageType getType()
	{
		return type;
	}
	
	/**
	 * Returns the message body.
	 * @return body the message body.
	 */
	public Object getBody()
	{
		return body;
	}
	

}
