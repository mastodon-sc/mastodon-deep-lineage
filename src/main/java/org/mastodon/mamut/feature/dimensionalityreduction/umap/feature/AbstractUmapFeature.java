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
package org.mastodon.mamut.feature.dimensionalityreduction.umap.feature;

import java.util.List;

import org.mastodon.graph.Vertex;
import org.mastodon.mamut.feature.dimensionalityreduction.AbstractOutputFeature;
import org.mastodon.properties.DoublePropertyMap;

/**
 * This generic feature is used to store the UMAP outputs.
 * <br>
 * The UMAP outputs are stored in a list of {@link DoublePropertyMap}s. The size of the list is equal to the number of dimensions of the UMAP output.
 */
public abstract class AbstractUmapFeature< V extends Vertex< ? > > extends AbstractOutputFeature< V >
{
	private static final String PROJECTION_NAME_TEMPLATE = "UMAP%d";

	protected static final String HELP_STRING =
			"Computes the UMAP according to the selected input dimensions, number of target dimensions, the minimum distance value and the number of neighbors.";

	protected AbstractUmapFeature( final List< DoublePropertyMap< V > > umapOutputMaps )
	{
		super( umapOutputMaps );
	}

	@Override
	protected String getProjectionNameTemplate()
	{
		return PROJECTION_NAME_TEMPLATE;
	}
}
