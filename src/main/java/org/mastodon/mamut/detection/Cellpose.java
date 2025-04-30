package org.mastodon.mamut.detection;

import java.io.IOException;

public class Cellpose extends Segmentation3D
{
	private final MODEL_TYPE modelType;

	private double anisotropy = 1;

	private double cellprobThreshold = 0;

	private boolean is3D = true;

	public Cellpose( final MODEL_TYPE modelType ) throws IOException
	{
		super();
		this.modelType = modelType;
	}

	@Override
	String generateEnvFileContent()
	{
		return "name: cellpose\n"
				+ "channels:\n"
				+ "  - nvidia\n"
				+ "  - pytorch\n"
				+ "  - conda-forge\n"
				+ "dependencies:\n"
				+ "  - python=3.10\n"
				+ "  - pip\n"
				+ "  - pip:\n"
				+ "    - cellpose[gui]\n"
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
				+ "import appose" + "\n\n"
				+ "task.update(message=\"Imports completed\")" + "\n"
				+ "image_ndarray = image.ndarray()" + "\n"
				+ "task.update(message=\"Image converted, Image shape: \" + \",\".join(str(dim) for dim in image.shape))" + "\n"
				+ "model = models.Cellpose(model_type=\"" + modelType.getModelName() + "\", gpu=True)" + "\n\n"
				+ "task.update(message=\"Model loaded\")" + "\n"
				+ "\n\n"
				+ "segmentation, flows, styles, diams = model.eval("
				+ "image_ndarray, "
				+ "diameter=None, "
				+ "channels=[0, 0], "
				+ "do_3D=" + is3DParam() + ", "
				+ "anisotropy=" + anisotropyParam() + ", "
				+ "z_axis=0, "
				+ "normalize=True, "
				+ "batch_size=8, "
				+ "flow3D_smooth=0, "
				+ "cellprob_threshold=" + cellprobThreshold + ")" + "\n"
				+ "\n\n"
				+ "task.update(message=\"Segmentation completed, Segmentation shape: \" + \",\".join(str(dim) for dim in segmentation.shape))"
				+ "\n"
				+ "\n"
				+ "shared = appose.NDArray(image.dtype, image.shape)" + "\n"
				+ "task.update(message=\"NDArray created\")" + "\n"
				+ "shared.ndarray()[:] = segmentation" + "\n"
				+ "task.update(message=\"NDArray filled\")" + "\n"
				+ "task.outputs['label_image'] = shared" + "\n";

		// diameter=None,
		// channels=[0, 0],
		// do_3D=True,  # Enable 3D processing
		// anisotropy=5.0,  # find out from spim data and set to actual value, allow user to reset to 1.0 and inform user that actual anisotropy leads to better results, but is slower
		// z_axis=0, # find out from spim data
		// normalize=True,
		// batch_size=8,
		// flow3D_smooth=0, # if do_3D and flow3D_smooth>0, smooth flows with gaussian filter of this stddev. Defaults to 0.
		// cellprob_threshold=3 # allow user input between 0 and 10 (float, optional): all pixels with value above threshold kept for masks, decrease to find more and larger masks. Defaults to 0.0.
	}

	public enum MODEL_TYPE
	{
		CYTO3( "cyto3" ),
		NUCLEI( "nuclei" ),
		CYTO2_CP3( "cyto2_cp3" ),
		TISSUENET_CP3( "tissuenet_cp3" ),
		LIVECELL_CP3( "livecell_cp3" ),
		YEAST_PHCP3( "yeast_PhC_cp3" ),
		YEAST_BFCP3( "yeast_BF_cp3" ),
		BACT_PHASE_CP3( "bact_phase_cp3" ),
		BACT_FLUOR_CP3( "bact_fluor_cp3" ),
		DEEPBACS_CP3( "deepbacs_cp3" ),
		CYTO2( "cyto2" ),
		CYTO( "cyto" ),
		CPX( "CPx" ),
		TRANSFORMER_CP3( "transformer_cp3" ),
		NEURIPS_CELLPOSE_DEFAULT( "neurips_cellpose_default" ),
		NEURIPS_CELLPOSE_TRANSFORMER( "neurips_cellpose_transformer" ),
		NEURIPS_GRAYSCALE_CYTO2( "neurips_grayscale_cyto2" ),
		CP( "CP" ),
		CPX2( "CPx" ),
		TN1( "TN1" ),
		TN2( "TN2" ),
		TN3( "TN3" ),
		LC1( "LC1" ),
		LC2( "LC2" ),
		LC3( "LC3" ),
		LC4( "LC4" );

		private final String modelName;

		MODEL_TYPE( final String modelName )
		{
			this.modelName = modelName;
		}

		public String getModelName()
		{
			return modelName;
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
}
