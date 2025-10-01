package org.mastodon.mamut.linking.trackastra;

public class TrackastraLinkingException extends Exception
{
	public TrackastraLinkingException( String message )
	{
		super( message );
	}

	public TrackastraLinkingException( String message, Throwable cause )
	{
		super( message, cause );
	}
}
