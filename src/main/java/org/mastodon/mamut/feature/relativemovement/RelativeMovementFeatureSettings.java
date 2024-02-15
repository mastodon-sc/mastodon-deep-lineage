package org.mastodon.mamut.feature.relativemovement;

public class RelativeMovementFeatureSettings
{
	private static final int DEFAULT_NUMBER_OF_NEIGHBOURS = 5;

	public final int numberOfNeighbors;

	public RelativeMovementFeatureSettings()
	{
		this( DEFAULT_NUMBER_OF_NEIGHBOURS );
	}

	public RelativeMovementFeatureSettings( final int numberOfNeighbors )
	{
		this.numberOfNeighbors = numberOfNeighbors;
	}

	@Override
	public boolean equals( final Object o )
	{
		if ( this == o )
			return true;
		if ( o == null || getClass() != o.getClass() )
			return false;

		final RelativeMovementFeatureSettings that = ( RelativeMovementFeatureSettings ) o;

		return numberOfNeighbors == that.numberOfNeighbors;
	}

	@Override
	public int hashCode()
	{
		// We only have one parameter, so we can use it directly.
		return numberOfNeighbors;
	}
}
