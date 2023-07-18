package org.mastodon.mamut.treesimilarity;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import org.junit.Test;
import org.mastodon.mamut.treesimilarity.tree.SimpleTreeExamples;
import org.mastodon.mamut.treesimilarity.tree.Tree;
import org.mastodon.mamut.treesimilarity.tree.TreeUtils;

public class NodeMappingTest
{

	private static final BiFunction< Double, Double, Double > DEFAULT_COSTS = ( o1, o2 ) -> {
		if ( o2 == null )
			return o1;
		else
			return Math.abs( o1 - o2 );
	};

	@Test
	public void test()
	{
		Tree< Double > tree1 = SimpleTreeExamples.tree1();
		Tree< Double > tree2 = SimpleTreeExamples.tree2();
		Map< Tree< Double >, Tree< Double > > mapping = ZhangUnorderedTreeEditDistance.nodeMapping( tree1, tree2, DEFAULT_COSTS );
		assertEquals( "10->10, 20->30, 30->20", asString( mapping ) );
		assertEquals( 20, computeCosts( tree1, tree2, mapping ), 0.0 );
	}

	private double computeCosts( Tree< Double > tree1, Tree< Double > tree2, Map< Tree< Double >, Tree< Double > > mapping )
	{
		Set< Tree< Double > > keys = mapping.keySet();
		Set< Tree< Double > > values = new HashSet<>( mapping.values() );
		double costs = 0;
		for ( Tree< Double > subtree : TreeUtils.listOfSubtrees( tree1 ) )
			if ( !keys.contains( subtree ) )
				costs += DEFAULT_COSTS.apply( subtree.getAttribute(), null );
		for ( Tree< Double > subtree : TreeUtils.listOfSubtrees( tree2 ) )
			if ( !values.contains( subtree ) )
				costs += DEFAULT_COSTS.apply( subtree.getAttribute(), null );
		for ( Map.Entry< Tree< Double >, Tree< Double > > entry : mapping.entrySet() )
			costs += DEFAULT_COSTS.apply( entry.getKey().getAttribute(), entry.getValue().getAttribute() );
		return costs;
	}

	private String asString( Map< Tree< Double >, Tree< Double > > mapping )
	{
		ArrayList< String > strings = new ArrayList<>();
		mapping.forEach( ( key, value ) -> strings.add( Math.round( key.getAttribute() ) + "->" + Math.round( value.getAttribute() ) ) );
		Collections.sort( strings );
		return String.join( ", ", strings );
	}

	private void assertAttributeMapping( String expected, Map< Tree< Double >, Tree< Double > > mapping )
	{
		Map< Double, Double > attributeMapping = new HashMap<>();
		mapping.forEach( ( key, value ) -> attributeMapping.put( key.getAttribute(), value.getAttribute() ) );
		for ( String pair : expected.split( ", *" ) )
		{
			String[] split = pair.split( "->" );
			double keyAttribute = Double.parseDouble( split[ 0 ] );
			double valueAttribute = Double.parseDouble( split[ 1 ] );
			assertEquals( valueAttribute, attributeMapping.get( keyAttribute ), 0.0 );
		}
	}
}
