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

    public static CropCriteria getByName(final String name) {
        for (final CropCriteria criteria : values())
            if (criteria.getName().equals(name))
                return criteria;

        return null;
    }

    public String getName()
	{
		return name;
	}
}
