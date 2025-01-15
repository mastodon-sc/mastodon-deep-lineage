/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2025 Stefan Hahmann
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
package org.mastodon.mamut.feature.relativemovement;

/**
 * Settings for relative movement feature.
 */
public class RelativeMovementFeatureSettings
{
	private static final int DEFAULT_NUMBER_OF_NEIGHBOURS = 5;

	public final int numberOfNeighbors;

	/**
	 * Default constructor. Uses {@link #DEFAULT_NUMBER_OF_NEIGHBOURS}.
	 */
	public RelativeMovementFeatureSettings()
	{
		this( DEFAULT_NUMBER_OF_NEIGHBOURS );
	}

	/**
	 * Constructor with number of neighbors.
	 *
	 * @param numberOfNeighbors the number of neighbors to consider for relative movement.
	 */
	public RelativeMovementFeatureSettings( final int numberOfNeighbors )
	{
		this.numberOfNeighbors = numberOfNeighbors;
	}

	@Override
	public boolean equals( final Object o )
	{
		if ( this == o )
			return true;
		if ( o == null || getClass() != o.getClass() )
			return false;

		final RelativeMovementFeatureSettings that = ( RelativeMovementFeatureSettings ) o;

		return numberOfNeighbors == that.numberOfNeighbors;
	}

	@Override
	public int hashCode()
	{
		// We only have one parameter, so we can use it directly.
		return numberOfNeighbors;
	}

	@Override
	public String toString()
	{
		return "RelativeMovementFeatureSettings{" + "numberOfNeighbors=" + numberOfNeighbors + "}";
	}
}
