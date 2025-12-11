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
package org.mastodon.mamut.detection.stardist;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandles;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import javax.annotation.Nullable;

import net.imglib2.util.Cast;

import org.apposed.appose.Service;
import org.mastodon.mamut.detection.Segmentation;
import org.mastodon.mamut.util.ResourceUtils;
import org.mastodon.mamut.util.ZipUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.bioimage.modelrunner.bioimageio.BioimageioRepo;
import io.bioimage.modelrunner.bioimageio.description.ModelDescriptor;
import io.bioimage.modelrunner.utils.JSONUtils;

/**
 * The StarDist class is responsible for performing segmentation tasks using the
 * StarDist model. It extends the Segmentation class and supports both 2D and 3D
 * data operations, depending on the chosen model type.<br>
 * This class initializes the StarDist model, downloads the required model files
 * if necessary, and configures the runtime environment.<br>
 * Additionally, it handles segmentation predictions while allowing adjustments to important thresholds
 * such as probability and non-maximum suppression.
 */
public class StarDist extends Segmentation
{
	public static final String ENV_NAME = "stardist";

	public static final String ENV_FILE_CONTENT =
			ResourceUtils.readResourceAsString( "org/mastodon/mamut/detection/stardist/stardist.toml", StarDist.class )
					.replace( "{ENV_NAME}", ENV_NAME )
					.replace( "{APPOSE_VERSION}", APPOSE_PYTHON_VERSION );

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private final ModelType modelType;

	private String installationFolderName;

	private boolean dataIs2D;

	private double probThresh;

	private double nmsThresh;

	private double estimatedDiameterXY;

	private double estimatedDiameterZ;

	private double expectedDiameterXY;

	private double expectedDiameterZ;

	public static final double DEFAULT_PROB_THRESHOLD = 0.5d;

	public static final double DEFAULT_NMS_THRESHOLD = 0.4d;

	public static final double DEFAULT_ESTIMATED_DIAMETER_XY = 30.0d;

	public static final double DEFAULT_ESTIMATED_DIAMETER_Z = 10.0d;

	public static final double DEFAULT_EXPECTED_DIAMETER_XY = 30.0d;

	public static final double DEFAULT_EXPECTED_DIAMETER_Z = 10.0d;

	public StarDist( final ModelType model, final Service python, final @Nullable org.scijava.log.Logger scijavaLogger )
			throws IOException, InterruptedException
	{
		super( python, scijavaLogger );
		logger.info( "Initializing StarDist, model: {}", model );
		this.modelType = model;
		this.probThresh = DEFAULT_PROB_THRESHOLD;
		this.nmsThresh = DEFAULT_NMS_THRESHOLD;
		this.estimatedDiameterXY = DEFAULT_ESTIMATED_DIAMETER_XY;
		this.estimatedDiameterZ = DEFAULT_ESTIMATED_DIAMETER_Z;
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
				if ( modelType.getUrl() == null )
					downloadFromBioimageIORepo( directory );
				else
				{
					installationFolderName = "model";
					String path = directory.getAbsolutePath() + File.separator + installationFolderName;
					logger.info( "Downloading model from URL: {} to {}", modelType.getUrl(), path );
					ZipUtils.downloadAndUnpack( modelType.getUrl(), Paths.get( path ) );
				}

			}
			catch ( IllegalArgumentException e )
			{
				logger.info( "Exception while downloading model: {}", e.getMessage() );
			}
		}
	}

	private void downloadFromBioimageIORepo( final File directory ) throws InterruptedException, IOException
	{
		logger.info( "Downloading model to {}", directory.getAbsolutePath() );
		BioimageioRepo repo = BioimageioRepo.connect();
		ModelDescriptor descriptor = repo.selectByName( modelType.getModelName() );
		String installationFolder = repo.downloadByName( modelType.getModelName(), directory.getAbsolutePath() );
		installationFolderName = Paths.get( installationFolder ).getFileName().toString();
		createConfigFromBioimageio( descriptor, directory.getAbsolutePath() + File.separator + installationFolderName );
		logger.info( "Downloading finished. Installation folder: {}", installationFolderName );
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
	protected String generateScript()
	{
		String axes = dataIs2D ? "YX" : "ZYX";
		String model = getModelString();
		logger.info( "Using star dist model: {}", model );
		return ResourceUtils.readResourceAsString( "org/mastodon/mamut/detection/stardist/stardist.py", StarDist.class )
				.replace( "{AXES}", axes )
				.replace( "{AXES_NORMALIZE}", dataIs2D ? "(0, 1)" : "(0, 1, 2)" )
				.replace( "{MODEL}", model )
				.replace( "{NMS_THRESH}", String.valueOf( nmsThresh ) )
				.replace( "{PROB_THRESH}", String.valueOf( probThresh ) )
				.replace( "{ESTIMATED_DIAMETER_XY}", String.valueOf( estimatedDiameterXY ) )
				.replace( "{ESTIMATED_DIAMETER_Z}", String.valueOf( estimatedDiameterZ ) )
				.replace( "{EXPECTED_DIAMETER_XY}", String.valueOf( expectedDiameterXY ) )
				.replace( "{EXPECTED_DIAMETER_Z}", String.valueOf( expectedDiameterZ ) );
	}

	private String getModelString()
	{
		String baseDir = "models" + File.separator + modelType.getModelPath();
		if ( modelType.getModelPath() == null )
		{
			if ( dataIs2D )
				return "model = StarDist2D.from_pretrained('2D_demo')";
			else
				return "model = StarDist3D.from_pretrained('3D_demo')";
		}
		else
		{
			if ( dataIs2D )
				return "model = StarDist2D(None, name='" + installationFolderName + "', basedir=r'" + baseDir + "')";
			else
				return "model = StarDist3D(None, name='" + installationFolderName + "', basedir=r'" + baseDir + "')";
		}
	}

	public static String generateImportStatements( final ModelType modelType, final boolean dataIs2D )
	{
		String starDistImport;
		if ( modelType.getModelPath() == null )
		{
			if ( dataIs2D )
				starDistImport = "StarDist2D=StarDist2D";
			else
				starDistImport = "StarDist3D=StarDist3D";
		}
		else if ( Boolean.TRUE.equals( modelType.is2D() ) )
			starDistImport = "StarDist2D=StarDist2D";
		else
			starDistImport = "StarDist3D=StarDist3D";

		return "import numpy as np" + "\n"
				+ "import appose as appose" + "\n"
				+ "from csbdeep.utils import normalize" + "\n"
				+ "import tensorflow as tf" + "\n"
				+ "from scipy.ndimage import zoom" + "\n"
				+ getImportStarDistCommand( modelType, dataIs2D )
				+ "\n"
				+ "task.update(message='Imports completed')" + "\n"
				+ "\n"
				+ "task.export(np=np, appose=appose, normalize=normalize, zoom=zoom, tf=tf, " + starDistImport + ")" + "\n";
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

	/**
	 * Set the estimated diameter (in pixel) for the XY plane.
	 *
	 * @param estimatedDiameterXY the estimated diameter in the XY plane
	 */
	public void setEstimatedDiameterXY( final double estimatedDiameterXY )
	{
		this.estimatedDiameterXY = estimatedDiameterXY;
	}

	/**
	 * Set the estimated diameter (in pixel) for the Z axis.
	 * @param estimatedDiameterZ the estimated diameter in the Z axis
	 */
	public void setEstimatedDiameterZ( final double estimatedDiameterZ )
	{
		this.estimatedDiameterZ = estimatedDiameterZ;
	}

	/**
	 * Set the diameter expected by the chosen model (in pixel) for the XY plane.
	 *
	 * @param expectedDiameterXY the expected diameter in the XY plane
	 */
	public void setExpectedDiameterXY( final double expectedDiameterXY )
	{
		this.expectedDiameterXY = expectedDiameterXY;
	}

	/**
	 * Set the diameter expected by the chosen model (in pixel) for the Z axis.
	 *
	 * @param expectedDiameterZ the expected diameter in the Z axis
	 */
	public void setExpectedDiameterZ( final double expectedDiameterZ )
	{
		this.expectedDiameterZ = expectedDiameterZ;
	}

	private static String getImportStarDistCommand( final ModelType modelType, final boolean dataIs2D )
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

	public enum ModelType
	{
		PLANT_NUCLEI_3D( "StarDist Plant Nuclei 3D ResNet", "plant-nuclei-3d", false, null ),
		FLUO_2D( "Fluorescence Nuclei Segmentation", "fluo-2d", true, null ),
		// H_E( "H&E Nuclei Segmentation", "h-e-nuclei", true, null ), // NB: operates on 3 input channels
		SOSPIM_3D( "SoSPIM Nuclei", "sospim-nuclei-3d", false,
				"https://zenodo.org/records/10518151/files/model_sospim.zip?download=1" ),
		CONFOCAL_3D( "Confocal Nuclei", "confocal-nuclei-3d", false,
				"https://zenodo.org/records/10518151/files/model_confocal.zip?download=1" ),
		SPINNING_DISK_3D( "Spinning Disk Nuclei", "spinning-disk-nuclei-3d", false,
				"https://zenodo.org/records/10518151/files/model_spinning.zip?download=1" ),
		DEMO( "Default Model", null, null, null );

		private final String modelName;

		private final String modelPath;

		private final Boolean is2D;

		private final String urlString;

		private final double expectedDiameterXY;

		private final double expectedDiameterZ;

		ModelType(
				final String modelName, final String modelPath, final Boolean is2D, final String urlString,
				final double expectedDiameterXY, final double expectedDiameterZ
		)
		{
			this.modelName = modelName;
			this.modelPath = modelPath;
			this.is2D = is2D;
			this.urlString = urlString;
			this.expectedDiameterXY = expectedDiameterXY;
			this.expectedDiameterZ = expectedDiameterZ;
		}

		public String getModelName()
		{
			return modelName;
		}

		public String getModelPath()
		{
			return modelPath;
		}

		public URL getUrl()
		{
			if ( urlString == null )
				return null;

			try
			{
				return URI.create( urlString ).toURL();
			}
			catch ( MalformedURLException e )
			{
				throw new IllegalStateException( "Invalid URL in enum: " + urlString, e );
			}
		}

		public Boolean is2D()
		{
			return is2D;
		}

		public double getExpectedDiameterXY()
		{
			return expectedDiameterXY;
		}

		public double getExpectedDiameterZ()
		{
			return expectedDiameterZ;
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

			return modelName.replace( "StarDist ", "" ) + dimensionality;
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
