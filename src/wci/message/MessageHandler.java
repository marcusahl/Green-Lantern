package wci.message;

import java.util.ArrayList;
import wci.message.*;

public class MessageHandler 
{
	private Message message;								// message
	private ArrayList<MessageListener> listeners;			// listener list

	public MessageHandler() 
	{
		this.listeners = new ArrayList<MessageListener>();
	}
	
	public void addListener(MessageListener listener)
	{
		listeners.add(listener);
	}
	
	public void removeListener(MessageListener listener)
	{
		listeners.remove(listener);
	}
	
	public void sendMessage(Message message)
	{
		this.message = message;
		notifyListeners();
	}
	
	private void notifyListeners()
	{
		for (MessageListener listener : listeners) 
		{
			listener.messageReceived(message);
		}
	}

}
