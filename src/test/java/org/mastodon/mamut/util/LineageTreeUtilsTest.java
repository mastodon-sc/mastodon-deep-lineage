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

import org.junit.Test;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph1;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph2;
import org.mastodon.mamut.model.Model;

import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class LineageTreeUtilsTest
{
	// TODO: remove after https://github.com/mastodon-sc/mastodon/pull/274 is merged
	@Test
	public void testGetMinTimepoint()
	{
		assertEquals( 0, LineageTreeUtils.getMinTimepoint( new ExampleGraph1().getModel() ) );
		assertEquals( 0, LineageTreeUtils.getMinTimepoint( new ExampleGraph2().getModel() ) );

	}

	// TODO: remove after https://github.com/mastodon-sc/mastodon/pull/274 is merged
	@Test
	public void testGetMaxTimepoint()
	{
		assertEquals( 3, LineageTreeUtils.getMaxTimepoint( new ExampleGraph1().getModel() ) );
		assertEquals( 7, LineageTreeUtils.getMaxTimepoint( new ExampleGraph2().getModel() ) );
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
}
