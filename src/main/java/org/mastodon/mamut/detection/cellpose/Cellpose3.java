package org.mastodon.mamut.detection.cellpose;

import java.io.IOException;

/**
 * Cellpose3 is a specialized implementation of the {@link Cellpose} class, specifically
 * designed to use Cellpose version 3 model for cell segmentation tasks.<br>
 * The type of model to use is specified via the {@link ModelType} enum during instantiation.
 */
public class Cellpose3 extends Cellpose
{
	private final ModelType modelType;
	private double anisotropy = 1;

	public Cellpose3( final ModelType modelType ) throws IOException
	{
		super();
		this.modelType = modelType;
	}

	public void setAnisotropy( final double anisotropy )
	{
		this.anisotropy = anisotropy;
	}

	private String anisotropyParam()
	{
		return is3D() ? String.valueOf( anisotropy ) : "1.0";
	}

	@Override
	protected String getLoadModelCommand()
	{
		if ( modelType.hasSizeModel() )
			return "model = models.Cellpose(model_type=\"" + modelType.getModelName() + "\", gpu=True)" + "\n";
		else
			return "model = models.CellposeModel(model_type=\"" + modelType.getModelName() + "\", gpu=True)" + "\n";
	}

	@Override
	protected String getEvaluateModelCommand()
	{
		String diams = modelType.hasSizeModel() ? ", diams" : "";
		return "segmentation, flows, styles" + diams + " = model.eval("
				+ "image_ndarray, "
				+ "diameter=" + getDiameter() + ", "
				+ "channels=[0, 0], "
				+ "do_3D=" + is3DParam() + ", "
				+ "anisotropy=" + anisotropyParam() + ", "
				+ "z_axis=0, "
				+ "normalize=True, "
				+ "batch_size=8, "
				+ "flow3D_smooth=0, "
				+ "flow_threshold=" + flowThreshold + ", "
				+ "cellprob_threshold=" + cellProbThreshold + ")" + "\n";
	}

	@Override
	protected String generateEnvFileContent()
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
				+ "    - git+https://github.com/apposed/appose-python.git@efe6dadb2242ca45820fcbb7aeea2096f99f9cb2\n" // contains a bug fix to run appose on Windows
				// + "    - appose\n"
				+ "  - pytorch\n"
				+ "  - pytorch-cuda\n"
				+ "  - numpy\n";
	}

	public enum ModelType
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

		ModelType( final String modelName, final boolean hasSizeModel )
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

		public static ModelType fromString( final String modelName )
		{
			for ( ModelType type : ModelType.values() )
			{
				if ( type.modelName.equalsIgnoreCase( modelName ) )
				{
					return type;
				}
			}
			throw new IllegalArgumentException( "No enum constant for model name: " + modelName );
		}
	}
}
