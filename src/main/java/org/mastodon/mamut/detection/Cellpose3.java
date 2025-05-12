package org.mastodon.mamut.detection;

import java.io.IOException;

public class Cellpose3 extends Segmentation3D
{
	private final MODEL_TYPE modelType;

	private double anisotropy = 1;

	private double cellprobThreshold = 0;

	private double flowThreshold = 0;

	private boolean is3D = true;

	public final static double DEFAULT_CELLPROB_THRESHOLD = 3d;

	public final static double DEFAULT_FLOW_THRESHOLD = 0.4d;

	public Cellpose3( final MODEL_TYPE modelType ) throws IOException
	{
		super();
		this.modelType = modelType;
	}

	@Override
	String generateEnvFileContent()
	{
		return "name: cellpose3\n"
				+ "channels:\n"
				+ "  - nvidia\n"
				+ "  - pytorch\n"
				+ "  - conda-forge\n"
				+ "dependencies:\n"
				+ "  - python=3.10\n"
				+ "  - pip\n"
				+ "  - pip:\n"
				+ "    - cellpose==3.1.1.2\n"
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

	public enum MODEL_TYPE
	{
		CYTO3( "cyto3", true ),
		NUCLEI( "nuclei", true ),
		CYTO2_CP3( "cyto2_cp3", false ),
		TISSUENET_CP3( "tissuenet_cp3", false ),
		LIVECELL_CP3( "livecell_cp3", false ),
		YEAST_PHCP3( "yeast_PhC_cp3", false ),
		YEAST_BFCP3( "yeast_BF_cp3", false ),
		BACT_PHASE_CP3( "bact_phase_cp3", false ),
		BACT_FLUOR_CP3( "bact_fluor_cp3", false ),
		DEEPBACS_CP3( "deepbacs_cp3", false ),
		CYTO2( "cyto2", true ),
		CYTO( "cyto", true ),
		CPX( "CPx", false ),
		NEURIPS_GRAYSCALE_CYTO2( "neurips_grayscale_cyto2", false ),
		CP( "CP", false ),
		CPX2( "CPx", false ),
		TN1( "TN1", false ),
		TN2( "TN2", false ),
		TN3( "TN3", false ),
		LC1( "LC1", false ),
		LC2( "LC2", false ),
		LC3( "LC3", false ),
		LC4( "LC4", false );

		private final String modelName;

		private final boolean hasSizeModel;

		MODEL_TYPE( final String modelName, final boolean hasSizeModel )
		{
			this.modelName = modelName;
			this.hasSizeModel = hasSizeModel;
		}

		public String getModelName()
		{
			return modelName;
		}

		public boolean hasSizeModel()
		{
			return hasSizeModel;
		}

		@Override
		public String toString()
		{
			return modelName;
		}

		public static MODEL_TYPE fromString( final String modelName )
		{
			for ( MODEL_TYPE type : MODEL_TYPE.values() )
			{
				if ( type.modelName.equalsIgnoreCase( modelName ) )
				{
					return type;
				}
			}
			throw new IllegalArgumentException( "No enum constant for model name: " + modelName );
		}
	}

	public double getAnisotropy()
	{
		return anisotropy;
	}

	public void setAnisotropy( final double anisotropy )
	{
		this.anisotropy = anisotropy;
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

	private String is3DParam()
	{
		return is3D ? "True" : "False";
	}

	private String anisotropyParam()
	{
		return is3D() ? String.valueOf( anisotropy ) : "1.0";
	}

	private String getLoadModelCommand()
	{
		if ( modelType.hasSizeModel() )
			return "model = models.Cellpose(model_type=\"" + modelType.getModelName() + "\", gpu=True)" + "\n";
		else
			return "model = models.CellposeModel(model_type=\"" + modelType.getModelName() + "\", gpu=True)" + "\n";
	}

	private String getEvaluateModelCommand()
	{
		String diams = modelType.hasSizeModel() ? ", diams" : "";
		return "segmentation, flows, styles" + diams + " = model.eval("
				+ "image_ndarray, "
				+ "diameter=None, "
				+ "channels=[0, 0], "
				+ "do_3D=" + is3DParam() + ", "
				+ "anisotropy=" + anisotropyParam() + ", "
				+ "z_axis=0, "
				+ "normalize=True, "
				+ "batch_size=8, "
				+ "flow3D_smooth=0, "
				+ "flow_threshold=" + flowThreshold + ", "
				+ "cellprob_threshold=" + cellprobThreshold + ")" + "\n";
	}
}
