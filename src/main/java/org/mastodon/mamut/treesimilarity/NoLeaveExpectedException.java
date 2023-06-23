package org.mastodon.mamut.treesimilarity;

public class NoLeaveExpectedException extends RuntimeException
{

	public NoLeaveExpectedException()
	{
		super( "At least 3 paired roots are needed to compute a coordinate transformation between the two datasets." );
	}

	public NoLeaveExpectedException( String message )
	{
		super( message );
	}
}
