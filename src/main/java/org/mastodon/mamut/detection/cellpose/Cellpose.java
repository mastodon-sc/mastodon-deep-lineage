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
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;

import org.apposed.appose.Service;
import org.mastodon.mamut.detection.Segmentation;

/**
 * The class contains the common functionality for Cellpose3- and Cellpose4-based cell segmentation<br>
 * It contains the configurable parameters {@code probability threshold},
 * {@code flow threshold}, {@code diameter}, and {@code 3D mode} toggling.<br>
 * Derived classes must implement {@link #generateScript()} to provide the Python script and populate inputs.<br>
 */
public abstract class Cellpose extends Segmentation
{
	protected double cellProbThreshold = 0;

	protected double flowThreshold = 0;

	protected double diameter = 0;

	protected boolean is3D = true;

	public static final double DEFAULT_CELLPROB_THRESHOLD = 3d;

	public static final double DEFAULT_FLOW_THRESHOLD = 0.4d;

	public static final double DEFAULT_DIAMETER = 0d;

	protected Cellpose( final Service python, final @Nullable org.scijava.log.Logger scijavaLogger ) throws IOException
	{
		super( python, scijavaLogger );
	}

	public void setCellProbThreshold( final double cellProbThreshold )
	{
		this.cellProbThreshold = cellProbThreshold;
	}

	public void setFlowThreshold( final double flowThreshold )
	{
		this.flowThreshold = flowThreshold;
	}

	public void setDiameter( final double diameter )
	{
		this.diameter = diameter;
	}

	public boolean is3D()
	{
		return is3D;
	}

	public void set3D( final boolean is3D )
	{
		this.is3D = is3D;
	}

	@Override
	protected String getOutputKey()
	{
		return "labels";
	}

	/**
	 * Loads a Python script resource from the cellpose-appose jar by classpath path.
	 */
	protected static String loadCellposeScript( final String path )
	{
		try
		{
			java.net.URL url = fiji.plugin.appose.cellpose.Cellpose.class.getResource( path );
			if ( url == null )
				throw new RuntimeException( path + " not found in classpath — is cellpose-appose on the classpath?" );
			return IOUtils.toString( url, StandardCharsets.UTF_8 );
		}
		catch ( IOException e )
		{
			throw new RuntimeException( "Failed to load " + path, e );
		}
	}

	/** Content of cp_utils.py from cellpose-appose, used as the Python service init script. */
	public static final String CP_UTILS_SCRIPT = loadCellposeScript( "/cp_utils.py" );
}
