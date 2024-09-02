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

/**
 * Settings for the UMAP feature.
 * <br>
 * Encapsulates the settings for the UMAP feature, such as:
 * <ul>
 *     <li>the number of neighbors to consider in the umap algorithm</li>
 *     <li>the minimum distance between points</li>
 *     <li>whether to standardize features</li>
 *     <li>the number of output dimensions</li>
 * </ul>
 */
public class UmapFeatureSettings
{
	public static final int DEFAULT_NUMBER_OF_OUTPUT_DIMENSIONS = 2;

	public static final int DEFAULT_NUMBER_OF_NEIGHBORS = 15;

	public static final double DEFAULT_MINIMUM_DISTANCE = 0.1d;

	public static final boolean DEFAULT_STANDARDIZE_FEATURES = true;

	private int numberOfOutputDimensions;

	private int numberOfNeighbors;

	private double minimumDistance;

	private boolean standardizeFeatures;

	/**
	 * Constructor with default values.
	 * Default values are:
	 * <ul>
	 *     <li>number of dimensions: {@value DEFAULT_NUMBER_OF_OUTPUT_DIMENSIONS}</li>
	 *     <li>number of neighbors: {@value DEFAULT_NUMBER_OF_NEIGHBORS}</li>
	 *     <li>minimum distance: {@value DEFAULT_MINIMUM_DISTANCE}</li>
	 *     <li>standardize features: {@value DEFAULT_STANDARDIZE_FEATURES}</li>
	 * </ul>
	 */
	public UmapFeatureSettings()
	{
		this( DEFAULT_NUMBER_OF_OUTPUT_DIMENSIONS, DEFAULT_NUMBER_OF_NEIGHBORS, DEFAULT_MINIMUM_DISTANCE, DEFAULT_STANDARDIZE_FEATURES );
	}

	/**
	 * Constructor with number of neighbors.
	 *
	 * @param numberOfOutputDimensions the number of neighbors to consider for relative movement.
	 */
	public UmapFeatureSettings( final int numberOfOutputDimensions, final int numberOfNeighbors, final double minimumDistance,
			final boolean standardizeFeatures )
	{
		this.numberOfOutputDimensions = numberOfOutputDimensions;
		this.numberOfNeighbors = numberOfNeighbors;
		this.minimumDistance = minimumDistance;
		this.standardizeFeatures = standardizeFeatures;
	}

	public int getNumberOfOutputDimensions()
	{
		return numberOfOutputDimensions;
	}

	public int getNumberOfNeighbors()
	{
		return numberOfNeighbors;
	}

	public double getMinimumDistance()
	{
		return minimumDistance;
	}

	public boolean isStandardizeFeatures()
	{
		return standardizeFeatures;
	}

	public void setNumberOfOutputDimensions( final int numberOfOutputDimensions )
	{
		this.numberOfOutputDimensions = numberOfOutputDimensions;
	}

	public void setNumberOfNeighbors( final int numberOfNeighbors )
	{
		this.numberOfNeighbors = numberOfNeighbors;
	}

	public void setMinimumDistance( final double minimumDistance )
	{
		this.minimumDistance = minimumDistance;
	}

	public void setStandardizeFeatures( final boolean standardizeFeatures )
	{
		this.standardizeFeatures = standardizeFeatures;
	}

	@Override
	public String toString()
	{
		return "UmapFeatureSettings{" + "numberOfOutputDimensions=" + numberOfOutputDimensions + ", numberOfNeighbors=" + numberOfNeighbors
				+ ", minimumDistance=" + minimumDistance + ", standardizeFeatures=" + standardizeFeatures + '}';
	}
}
