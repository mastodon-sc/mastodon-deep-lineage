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
}
