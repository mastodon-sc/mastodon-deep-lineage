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
package org.mastodon.mamut.detection.cellpose;

import java.io.IOException;

import org.apposed.appose.Service;
import org.mastodon.mamut.detection.Segmentation;

/**
 * The class contains the common functionality for Cellpose3- and Cellpose4-based cell segmentation<br>
 * It contains the configurable parameters {@code probability threshold},
 * {@code flow threshold}, {@code diameter}, and {@code 3D mode} toggling.<br>
 * Derived classes must implement methods to load and run the specific models.<br>
 */
public abstract class Cellpose extends Segmentation
{
	protected double cellProbThreshold = 0;

	protected double flowThreshold = 0;

	protected double diameter = 0;

	protected boolean is3D = true;

	protected int gpuID = 0;

	protected double gpuMemoryFraction = 0.5d;

	public static final double DEFAULT_CELLPROB_THRESHOLD = 3d;

	public static final double DEFAULT_FLOW_THRESHOLD = 0.4d;

	public static final double DEFAULT_DIAMETER = 0d;

	protected Cellpose( final Service python ) throws IOException
	{
		super( python );
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

	public void setGpuID( final int gpuID )
	{
		this.gpuID = gpuID;
	}

	public void setGpuMemoryFraction( final double gpuMemoryFraction )
	{
		this.gpuMemoryFraction = gpuMemoryFraction;
	}

	protected String is3DParam()
	{
		return is3D ? "True" : "False";
	}

	protected String getDiameter()
	{
		return diameter > 0 ? String.valueOf( diameter ) : "None";
	}

	public int getGpuID()
	{
		return gpuID;
	}

	public double getGpuMemoryFraction()
	{
		return gpuMemoryFraction;
	}

	protected abstract String getLoadModelCommand();

	protected abstract String getEvaluateModelCommand();

	@Override
	protected String generateScript()
	{
		return "if torch.cuda.is_available():" + "\n"
				+ "    torch.cuda.set_device(" + getGpuID() + ")" + "\n" // pick GPU explicitly
				+ "    torch.cuda.set_per_process_memory_fraction(" + getGpuMemoryFraction() + ", device=" + getGpuID() + ")" + "\n" // cap memory usage of that GPU
				+ "    device_id = torch.cuda.current_device()" + "\n"
				+ "    device_name = torch.cuda.get_device_name(device_id)" + "\n"
				+ "    task.update(message=\"Running cellpose on GPU: \" + str(device_id) + \" (\"+device_name+\")\")" + "\n"
				+ "else:" + "\n"
				+ "    task.update(message=\"Running cellpose on CPU\")" + "\n"
				+ "\n"
				+ "image_ndarray = image.ndarray()" + "\n"
				+ "task.update(message=\"Image converted, Image shape: \" + \",\".join(str(dim) for dim in image.shape))" + "\n"
				+ getLoadModelCommand()
				+ "task.update(message=\"Model loaded\")" + "\n"
				+ "\n"
				+ getEvaluateModelCommand()
				+ "\n"
				+ "task.update(message=\"Segmentation completed, Segmentation shape: \" + \",\".join(str(dim) for dim in segmentation.shape))"
				+ "\n"
				+ "\n"
				+ "shared = appose.NDArray(image.dtype, image.shape)" + "\n"
				+ "task.update(message=\"NDArray created\")" + "\n"
				+ "shared.ndarray()[:] = segmentation" + "\n"
				+ "task.update(message=\"NDArray filled\")" + "\n"
				+ "task.outputs['label_image'] = shared" + "\n";
	}

	public static String generateImportStatements()
	{
		return "import numpy as np" + "\n"
				+ "from cellpose import models" + "\n"
				+ "import appose" + "\n"
				+ "import torch" + "\n"
				+ "\n"
				+ "task.update(message=\"Imports completed\")" + "\n"
				+ "\n"
				+ "task.export(np=np, models=models, appose=appose, torch=torch)" + "\n";
	}
}
