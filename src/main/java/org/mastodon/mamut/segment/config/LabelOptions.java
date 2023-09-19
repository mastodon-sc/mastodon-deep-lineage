package org.mastodon.mamut.segment.config;

import java.util.NoSuchElementException;


public enum LabelOptions
{
	SPOT_ID( "Spot Id" ),
	BRANCH_SPOT_ID( "BranchSpot Id" );

	private final String name;

	LabelOptions( String name )
	{
		this.name = name;
	}

	public static LabelOptions getByName( final String name )
	{
		for ( final LabelOptions options : values() )
			if ( options.getName().equals( name ) )
				return options;
		throw new NoSuchElementException();
	}

	public String getName()
	{
		return name;
	}
}
