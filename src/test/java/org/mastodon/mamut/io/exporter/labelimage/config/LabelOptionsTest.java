/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2023 N/A
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
