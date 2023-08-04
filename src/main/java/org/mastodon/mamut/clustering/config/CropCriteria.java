package org.mastodon.mamut.clustering.config;

public enum CropCriteria
{
	TIMEPOINT( "Timepoint" ),
	NUMBER_OF_CELLS( "Number of cells" );

	private final String name;

	CropCriteria( String name )
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
