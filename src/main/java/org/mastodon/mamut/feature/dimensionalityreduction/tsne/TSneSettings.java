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
package org.mastodon.mamut.feature.dimensionalityreduction.tsne;

import java.lang.invoke.MethodHandles;

import org.scijava.prefs.PrefService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Settings for the t-SNE feature.
 * <br>
 * Encapsulates the settings for the t-SNE feature, such as:
 * <ul>
 *     <li>the perplexity to consider in the t-SNE algorithm</li>
 *     <li>the maximum number of iterations</li>
 * </ul>
 */
public class TSneSettings
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	public static final int DEFAULT_PERPLEXITY = 30;

	public static final int MIN_VALUE_PERPLEXITY = 5;

	public static final int MAX_VALUE_PERPLEXITY = 50;

	public static final int DEFAULT_MAX_ITERATIONS = 1000;

	public static final int MIN_VALUE_MAX_ITERATIONS = 250;

	public static final int MAX_VALUE_MAX_ITERATIONS = 5000;

	public static final int INITIAL_DIMENSIONS = 50; // 50 is the default value in the original t-SNE implementation. It is used to reduce the dimensionality of the input data using PCA before running t-SNE.

	public static final boolean USE_PCA = true;

	public static final double THETA = 0.5d;

	private int perplexity;

	private int maxIterations;

	private static final String PERPLEXITY_SETTING = "Perplexity";

	private static final String MAX_ITERATIONS_SETTING = "MaxIterations";

	/**
	 * Constructor with default values.
	 * Default values are:
	 * <ul>
	 *     <li>perplexity: {@value DEFAULT_PERPLEXITY}</li>
	 *     <li>maximum iterations: {@value DEFAULT_MAX_ITERATIONS}</li>
	 * </ul>
	 */
	public TSneSettings()
	{
		this( DEFAULT_PERPLEXITY, DEFAULT_MAX_ITERATIONS );
	}

	public TSneSettings( final int perplexity, final int maxIterations )
	{
		this.perplexity = perplexity;
		this.maxIterations = maxIterations;
	}

	public int getPerplexity()
	{
		return perplexity;
	}

	public int getMaxIterations()
	{
		return maxIterations;
	}

	public void setPerplexity( final int perplexity )
	{
		this.perplexity = perplexity;
	}

	public void setMaxIterations( final int maxIterations )
	{
		this.maxIterations = maxIterations;
	}

	public static TSneSettings loadSettingsFromPreferences( final PrefService prefs )
	{
		int perplexity = prefs == null ? TSneSettings.DEFAULT_PERPLEXITY
				: prefs.getInt( TSneSettings.class, PERPLEXITY_SETTING, TSneSettings.DEFAULT_PERPLEXITY );
		int maxIterations = prefs == null ? TSneSettings.DEFAULT_MAX_ITERATIONS
				: prefs.getInt( TSneSettings.class, MAX_ITERATIONS_SETTING, TSneSettings.DEFAULT_MAX_ITERATIONS );
		return new TSneSettings( perplexity, maxIterations );
	}

	/**
	 * Saves the UMAP settings to the user preferences.
	 */
	public void saveSettingsToPreferences( final PrefService prefs )
	{
		logger.debug( "Save t-SNE settings." );
		if ( prefs == null )
			return;
		prefs.put( TSneSettings.class, PERPLEXITY_SETTING, getPerplexity() );
		prefs.put( TSneSettings.class, MAX_ITERATIONS_SETTING, getMaxIterations() );
	}

	/**
	 * Checks if the perplexity is valid for the given number of dataset rows.
	 * @param rows the number of rows in the dataset
	 * @return {@code true} if the perplexity is valid, {@code false} otherwise
	 */
	public boolean isValidPerplexity( final int rows )
	{
		return ( double ) rows - 1 >= 3d * perplexity;
	}

	/**
	 * Returns the maximum valid perplexity for the given number of dataset rows.
	 * @param rows the number of rows in the dataset
	 * @return the maximum valid perplexity
	 */
	public int getMaxValidPerplexity( final int rows )
	{
		return ( int ) ( ( rows - 1 ) / 3d );
	}

	@Override
	public String toString()
	{
		return "TSneSettings{numberOfNeighbors=" + perplexity + ", minimumDistance=" + maxIterations + '}';
	}
}
