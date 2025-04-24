package org.mastodon.mamut.detection;

public class Cellpose extends Segmentation3D
{
	private final MODEL_TYPE modelType;

	public Cellpose( final MODEL_TYPE modelType )
	{
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
				+ "task.update(message=\"Image converted\")" + "\n"
				+ "model = models.Cellpose(model_type=\"" + modelType.getModelName() + "\", gpu=True)" + "\n\n"
				+ "task.update(message=\"Model loaded\")" + "\n"
				+ "\n\n"
				+ "segmentation, flows, styles, diams = model.eval(image_ndarray, diameter=None, channels=[0, 0], do_3D=True, anisotropy=1.0, z_axis=0, normalize=True)"
				+ "\n\n"
				+ "task.update(message=\"Segmentation completed\")" + "\n"
				+ "shared = appose.NDArray(image.dtype, image.shape)" + "\n"
				+ "task.update(message=\"NDArray created\")" + "\n"
				+ "shared.ndarray()[:] = segmentation" + "\n"
				+ "task.update(message=\"NDArray filled\")" + "\n"
				+ "task.outputs['label_image'] = shared" + "\n";
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
}
