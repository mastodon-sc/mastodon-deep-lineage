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
package org.mastodon.mamut.detection.util;

/**
 * This class encapsulates the following properties: minTimepoint, maxTimepoint, setupId and resolutionLevel
 */
public class SpimImageProperties
{

	private final int minTimepoint;

	private final int maxTimepoint;

	private final int setupId;

	private final int resolutionLevel;

	public SpimImageProperties( int minTimepoint, int maxTimepoint, int setupId, int resolutionLevel )
	{
		this.minTimepoint = minTimepoint;
		this.maxTimepoint = maxTimepoint;
		this.setupId = setupId;
		this.resolutionLevel = resolutionLevel;
	}

	public int getMinTimepoint()
	{
		return minTimepoint;
	}

	public int getMaxTimepoint()
	{
		return maxTimepoint;
	}

	public int getSetupId()
	{
		return setupId;
	}

	public int getResolutionLevel()
	{
		return resolutionLevel;
	}

	@Override
	public String toString()
	{
		return "SpimImageProperties{" +
				"minTimepoint=" + minTimepoint +
				", maxTimepoint=" + maxTimepoint +
				", setupId=" + setupId +
				", resolutionLevel=" + resolutionLevel +
				'}';
	}
}
