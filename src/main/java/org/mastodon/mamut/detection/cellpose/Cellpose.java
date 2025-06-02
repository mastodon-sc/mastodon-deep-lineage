package org.mastodon.mamut.detection.cellpose;

import java.io.IOException;

import org.mastodon.mamut.detection.Segmentation;

public abstract class Cellpose extends Segmentation
{
	protected double cellProbThreshold = 0;

	protected double flowThreshold = 0;

	protected boolean is3D = true;

	public static final double DEFAULT_CELLPROB_THRESHOLD = 3d;

	public static final double DEFAULT_FLOW_THRESHOLD = 0.4d;

	protected Cellpose() throws IOException
	{
		super();
	}

	public void setCellProbThreshold( final double cellProbThreshold )
	{
		this.cellProbThreshold = cellProbThreshold;
	}

	public void setFlowThreshold( final double flowThreshold )
	{
		this.flowThreshold = flowThreshold;
	}

	public boolean is3D()
	{
		return is3D;
	}

	public void set3D( final boolean is3D )
	{
		this.is3D = is3D;
	}

	protected String is3DParam()
	{
		return is3D ? "True" : "False";
	}

	protected abstract String getLoadModelCommand();

	protected abstract String getEvaluateModelCommand();

	@Override
	protected String generateScript()
	{
		return "import numpy as np" + "\n"
				+ "from cellpose import models" + "\n"
				+ "import appose" + "\n"
				+ "\n"
				+ "task.update(message=\"Imports completed\")" + "\n"
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
}
