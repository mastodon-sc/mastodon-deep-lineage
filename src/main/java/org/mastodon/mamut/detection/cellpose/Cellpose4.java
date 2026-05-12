/*-
 * #%L
 * mastodon-deep-lineage
 * %%
 * Copyright (C) 2022 - 2026 Stefan Hahmann
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
package org.mastodon.mamut.detection.cellpose;

import java.io.IOException;

import javax.annotation.Nullable;

import org.apposed.appose.Service;

/**
 * Cellpose4 is a specialized implementation of the {@link Cellpose} class, specifically
 * designed to use Cellpose version 4 (Cellpose-SAM) model for cell segmentation tasks.<br>
 */
public class Cellpose4 extends Cellpose
{
	private static final String CP4_SCRIPT = loadCellposeScript( "/cp4.py" );

	public Cellpose4( final Service python, final @Nullable org.scijava.log.Logger scijavaLogger ) throws IOException
	{
		super( python, scijavaLogger );
	}

	@Override
	protected String generateScript()
	{
		inputs.put( "diameter", diameter );
		inputs.put( "use_3D", is3D );
		inputs.put( "stitch_threshold", 0.0 );
		inputs.put( "z_axis", is3D ? 0 : null );
		inputs.put( "channel_axis", null );
		inputs.put( "time_axis", null );
		inputs.put( "anisotropy", 1.0 );
		inputs.put( "niter", null );
		inputs.put( "flow3D_smooth", 0 );
		inputs.put( "resample", true );
		inputs.put( "normalize", true );
		inputs.put( "flow_threshold", flowThreshold );
		inputs.put( "cellprob_threshold", cellProbThreshold );
		inputs.put( "min_size", 15 );
		inputs.put( "tile_overlap", 0.1 );
		inputs.put( "n_channels", 1 );
		inputs.put( "compute_flows", false );
		return CP4_SCRIPT;
	}
}
