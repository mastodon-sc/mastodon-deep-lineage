package org.mastodon.mamut.clustering.config;

import java.util.NoSuchElementException;

public enum CropCriteria
{
	TIMEPOINT( "Timepoint", "time" ),
	NUMBER_OF_CELLS( "Number of cells", "cells" );

	private final String name;

	private final String nameShort;

	CropCriteria( String name, String nameShort )
	{
		this.name = name;
		this.nameShort = nameShort;
	}

	public static CropCriteria getByName( final String name )
	{
		for ( final CropCriteria criteria : values() )
			if ( criteria.getName().equals( name ) )
				return criteria;

		throw new NoSuchElementException();
	}

	public String getName()
	{
		return name;
	}

	public String getNameShort()
	{
		return nameShort;
	}
}
