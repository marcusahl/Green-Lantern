package wci.backend;

import wci.intermediate.ICode;
import wci.intermediate.SymTab;
import wci.message.*;

/**
 * <h1>Backend</h1>
 * 
 *<p>The framework class that represent the back end component.</p>
 */

public abstract class Backend implements MessageProducer 
{
	
	protected static MessageHandler messageHandler;
	
	static {
		messageHandler = new MessageHandler();
	}
	
	protected SymTab symTab;
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
	
	/**
	 * Process the intermediate code and the symbol table generated by the parser.
	 * To be implemented by a compiler or interpreter subclass.
	 * @param iCode the intermediate code.
	 * @param symTab the symbol table.
	 * @throws Exception if an error occurred.
	 */
	public abstract void process(ICode iCode, SymTab symTab)
		throws Exception;
	
	/**
	 * 
	 * @return messageHandler.
	 */
	public MessageHandler getMessageHandler()
	{
		return messageHandler;
	}
	
	/**
	 * 
	 * @return iCode.
	 */
	public ICode getICode()
	{
		return iCode;
	}
	
	/**
	 * 
	 * @return symTab.
	 */
	public SymTab getSymTab()
	{
		return symTab;
	}

}
