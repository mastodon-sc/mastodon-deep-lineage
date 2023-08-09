package org.mastodon.mamut.clustering.config;

import java.util.NoSuchElementException;

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

		throw new NoSuchElementException();
    }

    public String getName()
	{
		return name;
	}
}
