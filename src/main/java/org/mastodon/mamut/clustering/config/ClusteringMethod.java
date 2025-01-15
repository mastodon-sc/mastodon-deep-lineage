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
package org.mastodon.mamut.clustering.config;

import com.apporiented.algorithm.clustering.CompleteLinkageStrategy;
import com.apporiented.algorithm.clustering.LinkageStrategy;
import com.apporiented.algorithm.clustering.SingleLinkageStrategy;
import org.mastodon.mamut.clustering.util.AverageLinkageUPGMAStrategy;

import java.util.NoSuchElementException;

public enum ClusteringMethod implements HasName
{
	AVERAGE_LINKAGE( "Average linkage", new AverageLinkageUPGMAStrategy() ),
	SINGLE_LINKAGE( "Single Linkage", new SingleLinkageStrategy() ),
	COMPLETE_LINKAGE( "Complete Linkage", new CompleteLinkageStrategy() );

	private final String name;

	private final LinkageStrategy linkageStrategy;

	ClusteringMethod( String name, LinkageStrategy linkageStrategy )
	{
		this.name = name;
		this.linkageStrategy = linkageStrategy;
	}

	public String getName()
	{
		return name;
	}

	public static ClusteringMethod getByName(final String name) {
		for (final ClusteringMethod method : values())
			if (method.getName().equals(name))
				return method;

		throw new NoSuchElementException();
	}

	public LinkageStrategy getLinkageStrategy()
	{
		return linkageStrategy;
	}
}
