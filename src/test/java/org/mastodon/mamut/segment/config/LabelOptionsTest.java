package org.mastodon.mamut.segment.config;

import org.junit.Test;

import static org.junit.Assert.*;

public class LabelOptionsTest
{

	@Test
	public void getByName()
	{
		assertEquals( LabelOptions.SPOT_ID, LabelOptions.getByName( "Spot Id" ) );
		assertEquals( LabelOptions.BRANCH_SPOT_ID, LabelOptions.getByName( "BranchSpot Id" ) );
	}

	@Test
	public void getName()
	{
		assertEquals( "Spot Id", LabelOptions.SPOT_ID.getName() );
		assertEquals( "BranchSpot Id", LabelOptions.BRANCH_SPOT_ID.getName() );
	}
}
