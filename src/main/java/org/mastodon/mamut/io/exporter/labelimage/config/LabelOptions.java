package org.mastodon.mamut.io.exporter.labelimage.config;

import java.util.NoSuchElementException;


public enum LabelOptions
{
	SPOT_ID( "Spot Id" ),

	BRANCH_SPOT_ID( "BranchSpot Id" ),

	TRACK_ID( "Track Id" );

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
