package org.mastodon.mamut.detection;

import java.io.IOException;

public abstract class Cellpose extends Segmentation3D
{
	protected double cellprobThreshold = 0;

	protected double flowThreshold = 0;

	protected boolean is3D = true;

	public final static double DEFAULT_CELLPROB_THRESHOLD = 3d;

	public final static double DEFAULT_FLOW_THRESHOLD = 0.4d;

	public Cellpose() throws IOException
	{
		super();
	}

	public double getCellprobThreshold()
	{
		return cellprobThreshold;
	}

	public void setCellprobThreshold( final double cellprobThreshold )
	{
		this.cellprobThreshold = cellprobThreshold;
	}

	public double getFlowThreshold()
	{
		return flowThreshold;
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
	String generateScript()
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
