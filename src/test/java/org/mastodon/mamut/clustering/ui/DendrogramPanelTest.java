package org.mastodon.mamut.clustering.ui;

import com.apporiented.algorithm.clustering.AverageLinkageStrategy;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.mastodon.mamut.clustering.ClusterData;
import org.mastodon.mamut.clustering.util.Classification;
import org.mastodon.mamut.clustering.util.ClusterUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DendrogramPanelTest
{
	@Test
	public void testCountZerosAfterDecimalPoint()
	{
		assertEquals( 0, DendrogramPanel.countZerosAfterDecimalPoint( 5 ) );
		assertEquals( 0, DendrogramPanel.countZerosAfterDecimalPoint( 0.1 ) );
		assertEquals( 1, DendrogramPanel.countZerosAfterDecimalPoint( 0.01 ) );
		assertEquals( 2, DendrogramPanel.countZerosAfterDecimalPoint( -0.003 ) );
	}

	@Test
	public void testDendrogramPanel()
	{
		Classification< String > classification =
				ClusterUtils.getClassificationByClassCount( ClusterData.names, ClusterData.fixedDistances, new AverageLinkageStrategy(),
						3
				);
		DendrogramPanel< String > dendrogramPanel = new DendrogramPanel<>( classification );
		assertNotNull( dendrogramPanel );
	}

	@Test
	public void testDendrogramPanelScalebar()
	{
		Classification< String > classification =
				ClusterUtils.getClassificationByClassCount( ClusterData.names, ClusterData.fixedDistances, new AverageLinkageStrategy(),
						3
				);
		DendrogramPanel< String > dendrogramPanel = new DendrogramPanel<>( classification );
		DendrogramPanel< String >.Scalebar scalebar =
				dendrogramPanel.new Scalebar( new DendrogramPanel.DisplayMetrics( 20, 59, 446, 6.19d ) );
		Set< String > expectedTickValues = new HashSet<>( Arrays.asList( "0", "7", "14", "21", "28", "35", "42", "49", "56", "63", "70" ) );
		Set< String > actualTickValues = scalebar.ticks.stream().map( Pair::getValue ).collect( Collectors.toSet() );
		assertEquals( 11, scalebar.ticks.size() );
		assertEquals( expectedTickValues, actualTickValues );
	}
}
