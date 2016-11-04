package wci.message;

public interface MessageListener 
{
	/**
	 * Called to receive a message send by a message producer
	 * @param message the message that was sent
	 */
	public void messageReceived(Message message);

}
