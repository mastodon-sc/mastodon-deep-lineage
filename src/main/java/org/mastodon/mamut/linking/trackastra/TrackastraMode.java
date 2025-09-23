package org.mastodon.mamut.linking.trackastra;

public enum TrackastraMode
{
	GREEDY( "greedy", "Greedy linking" ), GREEDY_NODIV( "gredy_nodiv", "Greedy linking without divisions" );

	private final String name;

	private final String description;

	TrackastraMode( final String name, final String description )
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

	public static TrackastraMode fromString( final String name )
	{
		for ( final TrackastraMode mode : values() )
			if ( mode.name.equalsIgnoreCase( name ) )
				return mode;
		return null;
	}
}
