package org.mastodon.mamut.segment.config;

import org.junit.Test;

import static org.junit.Assert.*;

public class SegmentationTaskTest
{

	@Test
	public void getByName()
	{
		assertEquals( SegmentationTask.SEGMENT_ELLIPSOIDS_BDV, SegmentationTask.getByName( "Show segmentation in BigDataViewer" ) );
	}

	@Test
	public void getName()
	{
		assertEquals( "Show segmentation in BigDataViewer", SegmentationTask.SEGMENT_ELLIPSOIDS_BDV.getName() );
		assertEquals( "Save segmentation result to file", SegmentationTask.SEGMENT_ELLIPSOIDS_IMAGEJ.getName() );
	}
}
