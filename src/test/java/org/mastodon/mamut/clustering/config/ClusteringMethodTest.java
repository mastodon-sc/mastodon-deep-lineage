package org.mastodon.mamut.clustering.config;

import org.junit.Test;

import static org.junit.Assert.*;

public class ClusteringMethodTest
{

	@Test
	public void testGetName()
	{
		assertEquals( "Average linkage", ClusteringMethod.AVERAGE_LINKAGE.getName() );
	}

	@Test
	public void testGetByName()
	{
		assertEquals( ClusteringMethod.AVERAGE_LINKAGE, ClusteringMethod.getByName( "Average linkage" ) );
	}
}
