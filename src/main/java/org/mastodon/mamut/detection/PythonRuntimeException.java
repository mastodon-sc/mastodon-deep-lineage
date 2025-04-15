package org.mastodon.mamut.detection;

public class PythonRuntimeException extends RuntimeException
{
	public PythonRuntimeException( final String message )
	{
		super( message );
	}

	public PythonRuntimeException( final String message, final Throwable cause )
	{
		super( message, cause );
	}

	public PythonRuntimeException( final Throwable cause )
	{
		super( cause );
	}
}
