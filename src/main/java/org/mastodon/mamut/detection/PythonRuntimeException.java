package org.mastodon.mamut.detection;

/**
 * Exception thrown when there is an error in the Python runtime environment
 * used for image segmentation or other operations.
 */
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
