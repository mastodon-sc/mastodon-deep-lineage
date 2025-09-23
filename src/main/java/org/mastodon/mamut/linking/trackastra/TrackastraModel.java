package org.mastodon.mamut.linking.trackastra;

public enum TrackastraModel
{
	CTC( true, "CTC", "Cell Tracking Challenge (2D+3D" ),
	GENERAL_2D( false, "general_2d", "General Model (2D)" );

	private final boolean is3D;

	private final String name;

	private final String displayName;

	TrackastraModel( final boolean is3D, final String name, final String displayName )
	{
		this.is3D = is3D;
		this.name = name;
		this.displayName = displayName;
	}

	public boolean is3D()
	{
		return is3D;
	}

	public String getName()
	{
		return name;
	}

	@Override
	public String toString()
	{
		return displayName;
	}

}
