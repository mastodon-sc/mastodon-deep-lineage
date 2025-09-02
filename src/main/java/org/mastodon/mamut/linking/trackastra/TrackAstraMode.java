package org.mastodon.mamut.linking.trackastra;

public enum TrackAstraMode
{
	GREEDY( "greedy", "Greedy linking" ), GREEDY_NODIV( "gredy_nodiv", "Greedy linking without divisions" );

	private final String name;

	private final String description;

	TrackAstraMode( final String name, final String description )
	{
		this.name = name;
		this.description = description;
	}

	@Override
	public String toString()
	{
		return description;
	}

	public String getName()
	{
		return name;
	}

	public static TrackAstraMode fromString( final String name )
	{
		for ( final TrackAstraMode mode : values() )
			if ( mode.name.equalsIgnoreCase( name ) )
				return mode;
		return null;
	}
}
