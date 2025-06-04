package org.mastodon.mamut.detection.stardist;

/**
 * Represents a custom unchecked exception thrown in cases
 * where an error specific to the StarDist model occurs.
 */
public class StarDistModelException extends RuntimeException
{
	public StarDistModelException( final String message )
	{
		super( message );
	}
}
