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
package org.mastodon.mamut.feature.dimensionalityreduction;

/**
 * Settings for the Dimensionality reduction
 * <br>
 * Encapsulates the common settings for the dimensionality reduction, such as:
 * <ul>
 *     <li>whether to standardize features</li>
 *     <li>the number of output dimensions</li>
 * </ul>
 */
public class DimensionalityReductionSettings
{
	public static final int DEFAULT_NUMBER_OF_OUTPUT_DIMENSIONS = 2;

	public static final boolean DEFAULT_STANDARDIZE_FEATURES = true;

	private int numberOfOutputDimensions;

	private boolean standardizeFeatures;

	/**
	 * Constructor with default values.
	 * Default values are:
	 * <ul>
	 *     <li>number of dimensions: {@value DEFAULT_NUMBER_OF_OUTPUT_DIMENSIONS}</li>
	 *     <li>standardize features: {@value DEFAULT_STANDARDIZE_FEATURES}</li>
	 * </ul>
	 */
	protected DimensionalityReductionSettings()
	{
		this( DEFAULT_NUMBER_OF_OUTPUT_DIMENSIONS, DEFAULT_STANDARDIZE_FEATURES );
	}

	/**
	 * Constructor with number of neighbors.
	 *
	 * @param numberOfOutputDimensions the number of neighbors to consider for relative movement.
	 */
	public DimensionalityReductionSettings( final int numberOfOutputDimensions, final boolean standardizeFeatures )
	{
		this.numberOfOutputDimensions = numberOfOutputDimensions;
		this.standardizeFeatures = standardizeFeatures;
	}

	public int getNumberOfOutputDimensions()
	{
		return numberOfOutputDimensions;
	}

	public boolean isStandardizeFeatures()
	{
		return standardizeFeatures;
	}

	public void setNumberOfOutputDimensions( final int numberOfOutputDimensions )
	{
		this.numberOfOutputDimensions = numberOfOutputDimensions;
	}

	public void setStandardizeFeatures( final boolean standardizeFeatures )
	{
		this.standardizeFeatures = standardizeFeatures;
	}

	@Override
	public String toString()
	{
		return "DimensionalityReductionSettings{" + "numberOfOutputDimensions=" + numberOfOutputDimensions + ", standardizeFeatures="
				+ standardizeFeatures + '}';
	}
}
