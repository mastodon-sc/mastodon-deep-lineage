package org.mastodon.mamut.util;

import org.junit.Test;

import java.util.function.BiFunction;

import static org.junit.Assert.assertEquals;

public class ZhangUnorderedTreeEditDistanceTest
{
	@Test
	public void testzhang_edit_distance()
	{
		Tree tree2 = getExampleTree2();
		Tree tree5 = getExampleTree5();

		int distance =
				ZhangUnorderedTreeEditDistance.zhang_edit_distance( tree2, tree5, "node_weight", new BiFunction< Object, Object, Integer >()
				{
					@Override
					public Integer apply( Object o1, Object o2 )
					{
						if ( o2 == null )
							return ( Integer ) o1;
						else
							return Math.abs( ( Integer ) o1 - ( Integer ) o2 );
					}
				}, false );
		assertEquals( 20, distance );
	}

	private Tree getExampleTree2()
	{
		//   					   node1(node_weight=20)
		//                ┌-─────────┴─────────────┐
		//                │                        │
		//              node2(node_weight=10)    node3(node_weight=30)
		Tree node1 = new Tree();
		node1.getMyAttributes().put( "node_weight", 20 );

		Tree node2 = new Tree();
		node2.addAttributeToId( "node_weight", 10 );
		node1.addSubtree( node2 );

		Tree node3 = new Tree();
		node3.addAttributeToId( "node_weight", 30 );
		node1.addSubtree( node3 );

		return node1;
	}

	private Tree getExampleTree5()
	{
		//  				       node1(node_weight=30)
		//                ┌-─────────┴─────────────┐
		//                │                        │
		//              node2(node_weight=10)    node3(node_weight=20)

		Tree node1 = new Tree();
		node1.addAttributeToId( "node_weight", 30 );

		Tree node2 = new Tree();
		node2.addAttributeToId( "node_weight", 10 );
		node1.addSubtree( node2 );

		Tree node3 = new Tree();
		node3.addAttributeToId( "node_weight", 20 );
		node1.addSubtree( node3 );

		return node1;
	}
}
