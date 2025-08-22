package org.mastodon.mamut.detection;

/**
 * Represents an exception that occurs during an inference process.
 * This exception extends RuntimeException and provides constructors
 * for initializing with a message, a cause, or both.
 */
public class InferenceException extends RuntimeException
{
	public InferenceException( final String message )
	{
		super( message );
	}

	public InferenceException( final String message, final Throwable cause )
	{
		super( message, cause );
	}

	public InferenceException( final Throwable cause )
	{
		super( cause );
	}
}
