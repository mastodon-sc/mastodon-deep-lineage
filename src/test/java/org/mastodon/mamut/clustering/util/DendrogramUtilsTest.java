package org.mastodon.mamut.clustering.util;

import com.apporiented.algorithm.clustering.Cluster;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class DendrogramUtilsTest
{

	@Test
	public void testCountZerosAfterDecimalPoint()
	{
		assertEquals( 0, DendrogramUtils.countZerosAfterDecimalPoint( 5 ) );
		assertEquals( 0, DendrogramUtils.countZerosAfterDecimalPoint( 0.1 ) );
		assertEquals( 1, DendrogramUtils.countZerosAfterDecimalPoint( 0.01 ) );
		assertEquals( 2, DendrogramUtils.countZerosAfterDecimalPoint( -0.003 ) );
	}

	@Test
	public void testMapLeaveNames()
	{
		Cluster cluster1 = new Cluster( "1" );
		Cluster cluster2 = new Cluster( "2" );
		Cluster cluster3 = new Cluster( "3" );
		cluster1.addChild( cluster2 );
		cluster1.addChild( cluster3 );

		Map< String, String > objectMapping = new HashMap<>();
		objectMapping.put( "1", "A" );
		objectMapping.put( "2", "B" );
		objectMapping.put( "3", "C" );

		DendrogramUtils.mapLeaveNames( cluster1, objectMapping );

		assertEquals( "1", cluster1.getName() ); // not a leaf -> not renamed
		assertEquals( "B", cluster2.getName() );
		assertEquals( "C", cluster3.getName() );

		DendrogramUtils.mapLeaveNames( cluster1, objectMapping );

		assertEquals( "1", cluster1.getName() ); // not a leaf -> not renamed
		assertEquals( "B", cluster2.getName() );
		assertEquals( "C", cluster3.getName() );
	}
}
