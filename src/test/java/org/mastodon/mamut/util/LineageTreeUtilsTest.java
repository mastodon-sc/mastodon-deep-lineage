/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2023 Stefan Hahmann
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.mamut.util;

import org.junit.Before;
import org.junit.Test;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefSet;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph2;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph4;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class LineageTreeUtilsTest
{
	private ExampleGraph2 graph2;

	private ExampleGraph4 graph4;

	@Before
	public void setUp()
	{
		graph2 = new ExampleGraph2();
		graph4 = new ExampleGraph4();
	}

	@Test
	public void testGetFirstTimepointWithNSpots()
	{
		ExampleGraph2 exampleGraph2 = new ExampleGraph2();
		Model model = exampleGraph2.getModel();

		assertEquals( 5, LineageTreeUtils.getFirstTimepointWithNSpots( model, 3 ) );
		assertEquals( 3, LineageTreeUtils.getFirstTimepointWithNSpots( model, 2 ) );
		assertThrows( NoSuchElementException.class, () -> LineageTreeUtils.getFirstTimepointWithNSpots( model, 5 ) );
	}

	@Test
	public void testIsRoot()
	{
		assertTrue( LineageTreeUtils.isRoot( graph2.spot0 ) );
		assertFalse( LineageTreeUtils.isRoot( graph2.spot1 ) );
		assertTrue( LineageTreeUtils.isRoot( graph2.branchSpotA ) );
		assertFalse( LineageTreeUtils.isRoot( graph2.branchSpotB ) );
		assertFalse( LineageTreeUtils.isRoot( graph2.branchSpotC ) );
		assertFalse( LineageTreeUtils.isRoot( graph2.branchSpotD ) );
		assertFalse( LineageTreeUtils.isRoot( graph2.branchSpotE ) );
	}

	@Test
	public void testGetBranchSpots()
	{
		RefSet< Spot > inputSpots = RefCollections.createRefSet( graph2.getModel().getGraph().vertices() );
		inputSpots.add( graph2.spot0 );
		inputSpots.add( graph2.spot5 );
		RefSet< Link > inputLinks = RefCollections.createRefSet( graph2.getModel().getGraph().edges() );
		RefSet< BranchSpot > actual = LineageTreeUtils.getBranchSpots( graph2.getModel(), inputSpots, inputLinks );
		Set< BranchSpot > expected = new HashSet< BranchSpot >()
		{
			{
				add( graph2.branchSpotA );
				add( graph2.branchSpotD );
			}
		};
		assertEquals( expected, actual );
	}

	@Test
	public void testGetBranchLinks()
	{
		RefSet< Link > inputLinks = RefCollections.createRefSet( graph4.getModel().getGraph().edges() );
		inputLinks.add( graph4.link0 );
		inputLinks.add( graph4.link1 );
		RefSet< BranchLink > actual = LineageTreeUtils.getBranchLinks( graph4.getModel(), inputLinks );
		Set< BranchLink > expected = new HashSet< BranchLink >()
		{
			{
				add( graph4.branchLink0 );
			}
		};
		assertEquals( expected, actual );
	}

	@Test
	public void testGetAllVertexSuccessors()
	{
		Set< BranchSpot > expected = new HashSet< BranchSpot >()
		{
			{
				add( graph2.branchSpotA );
				add( graph2.branchSpotB );
				add( graph2.branchSpotC );
				add( graph2.branchSpotD );
				add( graph2.branchSpotE );
			}
		};
		RefSet< BranchSpot > actual = LineageTreeUtils.getAllVertexSuccessors( graph2.branchSpotA, graph2.getModel().getBranchGraph() );
		assertEquals( expected, actual );

		expected = new HashSet< BranchSpot >()
		{
			{
				add( graph2.branchSpotB );
				add( graph2.branchSpotD );
				add( graph2.branchSpotE );
			}
		};
		actual = LineageTreeUtils.getAllVertexSuccessors( graph2.branchSpotB, graph2.getModel().getBranchGraph() );
		assertEquals( expected, actual );
	}

	@Test
	public void testGetAllEdgeSuccessors()
	{
		Set< BranchLink > expected = new HashSet< BranchLink >()
		{
			{
				add( graph4.branchLink0 );
				add( graph4.branchLink1 );
			}
		};
		RefSet< BranchLink > actual = LineageTreeUtils.getAllEdgeSuccessors( graph4.branchSpotA, graph4.getModel().getBranchGraph() );
		assertEquals( expected, actual );

		expected = new HashSet< BranchLink >()
		{
			{
				add( graph4.branchLink2 );
				add( graph4.branchLink3 );
			}
		};
		actual = LineageTreeUtils.getAllEdgeSuccessors( graph4.branchSpotD, graph4.getModel().getBranchGraph() );
		assertEquals( expected, actual );
	}
}
