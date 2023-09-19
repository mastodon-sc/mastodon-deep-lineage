package org.mastodon.mamut.segment.config;

import java.util.NoSuchElementException;

public enum SegmentationTask
{

	SEGMENT_ELLIPSOIDS_BDV( "Show segmentation in BigDataViewer" ),
	SEGMENT_ELLIPSOIDS_IMAGEJ( "Save segmentation result to file" );

	private final String name;

	SegmentationTask( String name )
	{
		this.name = name;
	}

	public static SegmentationTask getByName( final String name )
	{
		for ( final SegmentationTask task : values() )
			if ( task.getName().equals( name ) )
				return task;

		throw new NoSuchElementException();
	}

	public String getName()
	{
		return name;
	}
}
