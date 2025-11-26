package org.mastodon.mamut.linking.trackastra;

public enum TrackastraMode
{
	GREEDY( "greedy", "Greedy linking with divisions" ),
	GREEDY_NODIV( "greedy_nodiv", "Greedy linking without divisions" );

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
}
