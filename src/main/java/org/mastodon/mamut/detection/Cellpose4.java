package org.mastodon.mamut.detection;

import java.io.IOException;

public class Cellpose4 extends Segmentation3D
{
	private double cellprobThreshold = 0;

	private boolean is3D = true;

	public Cellpose4() throws IOException
	{
		super();
	}

	@Override
	String generateEnvFileContent()
	{
		return "name: cellpose4\n"
				+ "channels:\n"
				+ "  - nvidia\n"
				+ "  - pytorch\n"
				+ "  - conda-forge\n"
				+ "dependencies:\n"
				+ "  - python=3.10\n"
				+ "  - pip\n"
				+ "  - pip:\n"
				+ "    - cellpose==4.0.2\n"
				+ "    - appose\n"
				+ "  - pytorch\n"
				+ "  - pytorch-cuda=11.8\n"
				+ "  - numpy=2.0.2\n";
	}

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

	public double getCellprobThreshold()
	{
		return cellprobThreshold;
	}

	public void setCellprobThreshold( final double cellprobThreshold )
	{
		this.cellprobThreshold = cellprobThreshold;
	}

	public boolean is3D()
	{
		return is3D;
	}

	public void set3D( final boolean is3D )
	{
		this.is3D = is3D;
	}

	private String is3DParam()
	{
		return is3D ? "True" : "False";
	}

	private String getLoadModelCommand()
	{
		return "model = models.CellposeModel(gpu=True)" + "\n";
	}

	private String getEvaluateModelCommand()
	{
		return "segmentation, flows, styles = model.eval("
				+ "image_ndarray, "
				+ "diameter=None, "
				+ "do_3D=" + is3DParam() + ", "
				+ "z_axis=0, "
				+ "normalize=True, "
				+ "batch_size=8, "
				+ "flow3D_smooth=0, "
				+ "cellprob_threshold=" + cellprobThreshold + ")" + "\n";
	}
}
