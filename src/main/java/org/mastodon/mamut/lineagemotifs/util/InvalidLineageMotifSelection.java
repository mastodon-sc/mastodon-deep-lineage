package org.mastodon.mamut.lineagemotifs.util;

public class InvalidLineageMotifSelection extends RuntimeException
{

	private final String logMessage;

	private final String uiTitle;

	private final String uiMessage;

	public InvalidLineageMotifSelection( String logMessage, String uiTitle, String uiMessage )
	{
		super( logMessage ); // or just pass null to super()
		this.logMessage = logMessage;
		this.uiTitle = uiTitle;
		this.uiMessage = uiMessage;
	}

	public InvalidLineageMotifSelection( String logMessage, String uiTitle, String uiMessage, Throwable cause )
	{
		super( logMessage, cause );
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
