package wci.backend;

import wci.intermediate.*;
import wci.message.*;

public abstract class Backend implements MessageProducer 
{
	
	protected static MessageHandler messageHandler;
	
	static {
		messageHandler = new MessageHandler();
	}
	
	protected SymTabStack symTabStack;
	protected ICode iCode;
	
	@Override
	public void addMessageListener(MessageListener listener) 
	{
		messageHandler.addListener(listener);

	}

	@Override
	public void removeMessageListener(MessageListener listener) 
	{
		messageHandler.removeListener(listener);

	}

	@Override
	public void sendMessage(Message message) 
	{
		messageHandler.sendMessage(message);

	}
	
	public abstract void process(ICode iCode, SymTabStack symTabStack)
		throws Exception;
	
	public MessageHandler getMessageHandler()
	{
		return messageHandler;
	}

	public ICode getICode()
	{
		return iCode;
	}

	public SymTabStack getSymTabStack()
	{
		return symTabStack;
	}

}
