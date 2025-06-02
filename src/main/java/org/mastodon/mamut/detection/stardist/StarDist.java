package org.mastodon.mamut.detection.stardist;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import net.imglib2.util.Cast;

import org.mastodon.mamut.detection.Segmentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.bioimage.modelrunner.bioimageio.BioimageioRepo;
import io.bioimage.modelrunner.bioimageio.description.ModelDescriptor;
import io.bioimage.modelrunner.utils.JSONUtils;

public class StarDist extends Segmentation
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private final ModelType modelType;

	private String installationFolderName;

	private boolean dataIs2D;

	private double probThresh;

	private double nmsThresh;

	public static final double DEFAULT_PROB_THRESHOLD = 0.5d;

	public static final double DEFAULT_NMS_THRESHOLD = 0.4d;

	public StarDist( final ModelType model ) throws IOException, InterruptedException
	{
		super();
		logger.info( "Initializing StarDist, model: {}", model );
		this.modelType = model;
		this.probThresh = DEFAULT_PROB_THRESHOLD;
		this.nmsThresh = DEFAULT_NMS_THRESHOLD;
		Path starDistModelRoot = getStarDistModelRoot();
		if ( starDistModelRoot == null )
			logger.debug( "StarDist model path is null. This is normal for the built-in demo models" );
		else
		{
			File directory = starDistModelRoot.toFile();
			if ( !directory.exists() && !directory.mkdirs() )
				throw new UncheckedIOException( "Failed to create environment directory: " + directory, new IOException() );

			if ( directory.isDirectory() )
				checkIfModelsExistsAndDownloadIfNeeded( directory );
			else
				logger.error( "The specified path is not a directory: {}", directory.getAbsolutePath() );
		}
	}

	private void checkIfModelsExistsAndDownloadIfNeeded( final File directory ) throws InterruptedException, IOException
	{
		File[] files = directory.listFiles();
		if ( files != null && files.length > 0 )
		{
			logger.info( "Found {} files/directories in {}", files.length, directory.getAbsolutePath() );
			for ( File file : files )
				logger.debug( "File/Directory: {}", file.getName() );
			installationFolderName = Paths.get( files[ 0 ].getAbsolutePath() ).getFileName().toString();
			logger.info( "Reusing model in folder: {}", installationFolderName );
		}
		else
		{
			try
			{
				logger.info( "Downloading model to {}", directory.getAbsolutePath() );
				BioimageioRepo repo = BioimageioRepo.connect();
				ModelDescriptor descriptor = repo.selectByName( modelType.getModelName() );
				String installationFolder = repo.downloadByName( modelType.getModelName(), directory.getAbsolutePath() );
				installationFolderName = Paths.get( installationFolder ).getFileName().toString();
				createConfigFromBioimageio( descriptor, directory.getAbsolutePath() + File.separator + installationFolderName );
				logger.info( "Downloading finished. Installation folder: {}", installationFolderName );
			}
			catch ( IllegalArgumentException e )
			{
				logger.info( "Exception while downloading model: {}", e.getMessage() );
			}
		}
	}

	private Path getStarDistModelRoot()
	{
		if ( modelType.getModelPath() == null )
			return null;
		return Paths.get( System.getProperty( "user.home" ), ".local", "share", "appose", "stardist", "models", modelType.getModelPath() );
	}

	private static void createConfigFromBioimageio( final ModelDescriptor descriptor, final String modelDir )
			throws IOException
	{
		Map< String, Object > stardistMap = Cast.unchecked( descriptor.getConfig().getSpecMap().get( "stardist" ) );
		Map< String, Object > stardistConfig = Cast.unchecked( stardistMap.get( "config" ) );
		File jsonFile = new File( modelDir, "config.json" );
		logger.info( "Creating config.json file: {}", jsonFile.getAbsolutePath() );
		JSONUtils.writeJSONFile( jsonFile.getAbsolutePath(), stardistConfig );
	}

	@Override
	protected String generateEnvFileContent()
	{
		return "name: stardist\n"
				+ "channels:\n"
				+ "  - conda-forge\n"
				+ "dependencies:\n"
				+ "  - python=3.10\n"
				+ "  - cudatoolkit=11.2\n"
				+ "  - cudnn=8.1.0\n"
				+ "  - numpy<1.24\n"
				+ "  - pip\n"
				+ "  - pip:\n"
				+ "    - numpy<1.24\n"
				+ "    - tensorflow==2.10\n"
				+ "    - stardist==0.8.5\n"
				// + "    - appose\n";
				+ "    - git+https://github.com/apposed/appose-python.git@efe6dadb2242ca45820fcbb7aeea2096f99f9cb2\n"; // contains a bug fix to run appose on Windows
	}

	@Override
	protected String generateScript()
	{
		return "import numpy as np" + "\n"
				+ "import appose" + "\n"
				+ "from csbdeep.utils import normalize" + "\n"
				+ getImportStarDistCommand()
				+ "\n"
				+ "task.update(message=\"Imports completed\")" + "\n"
				+ "np.random.seed(6)" + "\n"
				+ getAxesNormalizeCommand()
				+ "\n"
				+ "task.update(message=\"Loading StarDist pretrained model\")" + "\n"
				+ getLoadModelCommand()
				+ "image_ndarray = image.ndarray()" + "\n"
				+ "image_normalized = normalize(image_ndarray, 1, 99.8, axis=axes_normalize)" + "\n"
				+ "task.update(message=\"Image shape:\" + str(image_normalized.shape))" + "\n"
				+ "\n"
				+ "guessed_tiles = model._guess_n_tiles(image_normalized)" + "\n"
				+ "task.update(message=\"Guessed tiles: \" + str(guessed_tiles))" + "\n"
				+ "\n"
				+ getPredictionCommand()
				+ "shared = appose.NDArray(image.dtype, image.shape)" + "\n"
				+ "shared.ndarray()[:] = label_image" + "\n"
				+ "task.outputs['label_image'] = shared" + "\n";
	}

	public ModelType getModelType()
	{
		return modelType;
	}

	public void setDataIs2D( final boolean dataIs2D )
	{
		this.dataIs2D = dataIs2D;
	}

	/**
	 * Probability/Score Threshold: Determine the number of object candidates to enter non-maximum suppression.
	 * Higher values lead to fewer segmented objects, but will likely avoid false positives.
	 *
	 * @param probThresh the probability threshold
	 */
	public void setProbThresh( final double probThresh )
	{
		this.probThresh = probThresh;
	}

	/**
	 * Overlap Threshold: Determine when two objects are considered the same during non-maximum suppression.
	 * Higher values allow segmented objects to overlap substantially.
	 *
	 * @param nmsThresh the non-maximum suppression threshold
	 */
	public void setNmsThresh( final double nmsThresh )
	{
		this.nmsThresh = nmsThresh;
	}

	private String getImportStarDistCommand()
	{
		if ( modelType.getModelPath() == null )
		{
			if ( dataIs2D )
				return "from stardist.models import StarDist2D" + "\n ";
			return "from stardist.models import StarDist3D" + "\n ";
		}
		if ( Boolean.TRUE.equals( modelType.is2D() ) )
			return "from stardist.models import StarDist2D" + "\n ";
		return "from stardist.models import StarDist3D" + "\n ";
	}

	private String getAxesNormalizeCommand()
	{
		return "axes_normalize = (0, 1, 2)" + "\n ";
	}

	private String getPredictionCommand()
	{
		return "label_image, details = model.predict_instances(image_normalized, axes='ZYX', n_tiles=guessed_tiles, nms_thresh=" + nmsThresh
				+ ", prob_thresh=" + probThresh + ")"
				+ "\n";
	}

	private String getLoadModelCommand()
	{
		if ( modelType.getModelPath() == null )
		{
			if ( dataIs2D )
				return "model = StarDist2D.from_pretrained('2D_demo')" + "\n";
			else
				return "model = StarDist3D.from_pretrained('3D_demo')" + "\n";
		}
		String starDistModel = Boolean.TRUE.equals( modelType.is2D() ) ? "StarDist2D" : "StarDist3D";
		return "model = " + starDistModel + "(None, name='" + installationFolderName + "', basedir=r\"models" + File.separator
				+ modelType.getModelPath() + "\")"
				+ "\n";
	}

	public enum ModelType
	{
		PLANT_NUCLEI_3D( "StarDist Plant Nuclei 3D ResNet", "stardist-plant-nuclei-3d", false ),
		FLUO_2D( "StarDist Fluorescence Nuclei Segmentation", "stardist-fluo-2d", true ),
		// H_E( "StarDist H&E Nuclei Segmentation", "stardist-h-e-nuclei", true ), // NB: operates on 3 input channels
		DEMO( "StarDist Demo", null, null );

		private final String modelName;

		private final String modelPath;

		private final Boolean is2D;

		ModelType( final String modelName, final String modelPath, final Boolean is2D )
		{
			this.modelName = modelName;
			this.modelPath = modelPath;
			this.is2D = is2D;
		}

		public String getModelName()
		{
			return modelName;
		}

		public String getModelPath()
		{
			return modelPath;
		}

		public Boolean is2D()
		{
			return is2D;
		}

		@Override
		public String toString()
		{
			return modelName;
		}

		public String getDisplayName()
		{
			String dimensionality;
			if ( is2D == null )
				dimensionality = " (2D/3D)";
			else if ( is2D )
				dimensionality = " (2D)";
			else
				dimensionality = " (3D)";

			return modelName + dimensionality;
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
