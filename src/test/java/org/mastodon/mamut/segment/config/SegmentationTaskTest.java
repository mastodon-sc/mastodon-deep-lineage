package org.mastodon.mamut.segment.config;

import org.junit.Test;

import java.util.NoSuchElementException;

import static org.junit.Assert.*;

public class SegmentationTaskTest
{

	@Test
	public void getByName()
	{
		assertEquals( SegmentationTask.SEGMENT_ELLIPSOIDS_BDV, SegmentationTask.getByName( "Show segmentation in BigDataViewer" ) );
		assertEquals( SegmentationTask.SEGMENT_ELLIPSOIDS_IMAGEJ, SegmentationTask.getByName( "Save segmentation result to file" ) );
		assertThrows( NoSuchElementException.class, () -> SegmentationTask.getByName( "Foo" ) );
	}

	@Test
	public void getName()
	{
		assertEquals( "Show segmentation in BigDataViewer", SegmentationTask.SEGMENT_ELLIPSOIDS_BDV.getName() );
		assertEquals( "Save segmentation result to file", SegmentationTask.SEGMENT_ELLIPSOIDS_IMAGEJ.getName() );
	}
}
