package org.mastodon.mamut.io.exporter.labelimage.config;

import org.junit.Test;
import org.mastodon.mamut.io.exporter.labelimage.config.LabelOptions;

import java.util.NoSuchElementException;

import static org.junit.Assert.*;

public class LabelOptionsTest
{

	@Test
	public void getByName()
	{
		assertEquals( LabelOptions.SPOT_ID, LabelOptions.getByName( "Spot Id" ) );
		assertEquals( LabelOptions.BRANCH_SPOT_ID, LabelOptions.getByName( "BranchSpot Id" ) );
		assertEquals( LabelOptions.TRACK_ID, LabelOptions.getByName( "Track Id" ) );
		assertThrows( NoSuchElementException.class, () -> LabelOptions.getByName( "Foo" ) );
	}

	@Test
	public void getName()
	{
		assertEquals( "Spot Id", LabelOptions.SPOT_ID.getName() );
		assertEquals( "BranchSpot Id", LabelOptions.BRANCH_SPOT_ID.getName() );
		assertEquals( "Track Id", LabelOptions.TRACK_ID.getName() );
	}
}
