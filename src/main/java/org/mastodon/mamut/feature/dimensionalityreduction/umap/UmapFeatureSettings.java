/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2024 Stefan Hahmann
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
package org.mastodon.mamut.feature.dimensionalityreduction.umap;

import org.mastodon.mamut.feature.dimensionalityreduction.DimensionalityReductionSettings;

/**
 * Settings for the UMAP feature.
 * <br>
 * Encapsulates the settings for the UMAP feature, such as:
 * <ul>
 *     <li>the number of neighbors to consider in the umap algorithm</li>
 *     <li>the minimum distance between points</li>
 * </ul>
 */
public class UmapFeatureSettings extends DimensionalityReductionSettings
{
	public static final int DEFAULT_NUMBER_OF_NEIGHBORS = 15;

	public static final double DEFAULT_MINIMUM_DISTANCE = 0.1d;

	private int numberOfNeighbors;

	private double minimumDistance;

	/**
	 * Constructor with default values.
	 * Default values are:
	 * <ul>
	 *     <li>number of neighbors: {@value DEFAULT_NUMBER_OF_NEIGHBORS}</li>
	 *     <li>minimum distance: {@value DEFAULT_MINIMUM_DISTANCE}</li>
	 * </ul>
	 */
	public UmapFeatureSettings()
	{
		this( DEFAULT_NUMBER_OF_NEIGHBORS, DEFAULT_MINIMUM_DISTANCE );
	}

	public UmapFeatureSettings( final int numberOfNeighbors, final double minimumDistance )
	{
		super();
		this.numberOfNeighbors = numberOfNeighbors;
		this.minimumDistance = minimumDistance;
	}

	public UmapFeatureSettings( final int numberOfOutputDimensions, final int numberOfNeighbors, final double minimumDistance,
			final boolean standardizeFeatures )
	{
		super( numberOfOutputDimensions, standardizeFeatures );
		this.numberOfNeighbors = numberOfNeighbors;
		this.minimumDistance = minimumDistance;
	}

	public int getNumberOfNeighbors()
	{
		return numberOfNeighbors;
	}

	public double getMinimumDistance()
	{
		return minimumDistance;
	}

	public void setNumberOfNeighbors( final int numberOfNeighbors )
	{
		this.numberOfNeighbors = numberOfNeighbors;
	}

	public void setMinimumDistance( final double minimumDistance )
	{
		this.minimumDistance = minimumDistance;
	}

	@Override
	public String toString()
	{
		return super.toString() + ", UmapFeatureSettings{numberOfNeighbors=" + numberOfNeighbors + ", minimumDistance=" + minimumDistance
				+ '}';
	}
}
