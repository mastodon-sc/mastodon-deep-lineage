package org.mastodon.mamut.feature.spot.relativemovement;

public class SpotRelativeMovementFeatureSettings
{
	private static final int DEFAULT_NUMBER_OF_NEIGHBOURS = 5;

	final int numberOfNeighbors;

	public SpotRelativeMovementFeatureSettings()
	{
		this( DEFAULT_NUMBER_OF_NEIGHBOURS );
	}

	public SpotRelativeMovementFeatureSettings( final int numberOfNeighbors )
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

		final SpotRelativeMovementFeatureSettings that = ( SpotRelativeMovementFeatureSettings ) o;

		return numberOfNeighbors == that.numberOfNeighbors;
	}

	@Override
	public int hashCode()
	{
		// We only have one parameter, so we can use it directly.
		return numberOfNeighbors;
	}
}
