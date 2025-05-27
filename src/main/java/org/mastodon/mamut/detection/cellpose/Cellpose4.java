package org.mastodon.mamut.detection.cellpose;

import java.io.IOException;

public class Cellpose4 extends Cellpose
{
	public Cellpose4() throws IOException
	{
		super();
	}

	@Override
	protected String getLoadModelCommand()
	{
		return "model = models.CellposeModel(gpu=True)" + "\n";
	}

	@Override
	protected String getEvaluateModelCommand()
	{
		return "segmentation, flows, styles = model.eval("
				+ "image_ndarray, "
				+ "diameter=None, "
				+ "do_3D=" + is3DParam() + ", "
				+ "z_axis=0, "
				+ "normalize=True, "
				+ "batch_size=8, "
				+ "flow3D_smooth=0, "
				+ "flow_threshold=" + flowThreshold + ", "
				+ "cellprob_threshold=" + cellprobThreshold + ")" + "\n";
	}

	@Override
	protected String generateEnvFileContent()
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
				+ "  - pytorch-cuda\n"
				+ "  - numpy\n";
	}
}
