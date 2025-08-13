package org.mastodon.mamut.lineagemotifs.util;

public class InvalidLineageMotifException extends RuntimeException
{

	private final String logMessage;

	private final String uiTitle;

	private final String uiMessage;

	public InvalidLineageMotifException( String logMessage, String uiTitle, String uiMessage )
	{
		super( logMessage ); // or just pass null to super()
		this.logMessage = logMessage;
		this.uiTitle = uiTitle;
		this.uiMessage = uiMessage;
	}

	public String getLogMessage()
	{
		return logMessage;
	}

	public String getUiTitle()
	{
		return uiTitle;
	}

	public String getUiMessage()
	{
		return uiMessage;
	}
}
