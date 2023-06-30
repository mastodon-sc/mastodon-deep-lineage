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

	public String getName()
	{
		return name;
	}

	public static CropCriteria getByName( String name )
	{
		for ( CropCriteria cropCriterion : CropCriteria.values() )
			if ( cropCriterion.getName().equals( name ) )
				return cropCriterion;
		throw new IllegalArgumentException( "No enum constant with name: " + name );
	}
}
