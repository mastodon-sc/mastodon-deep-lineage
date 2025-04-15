package org.mastodon.mamut.detection;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;

import org.apposed.appose.Appose;
import org.apposed.appose.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StarDist3D
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	// private constructor to prevent instantiation
	private StarDist3D()
	{}

	static Environment setUpEnv() throws IOException
	{
		Environment env;
		try
		{
			File envFile = Files.createTempFile( "env", "yml" ).toFile();
			try (BufferedWriter writer = new BufferedWriter( new FileWriter( envFile ) ))
			{
				writer.write( "name: stardist\n" );
				writer.write( "channels:\n" );
				writer.write( "  - conda-forge\n" );
				writer.write( "dependencies:\n" );
				writer.write( "  - python=3.10\n" );
				writer.write( "  - cudatoolkit=11.2\n" );
				writer.write( "  - cudnn=8.1.0\n" );
				writer.write( "  - numpy<1.24\n" );
				writer.write( "  - pip\n" );
				writer.write( "  - pip:\n" );
				writer.write( "    - numpy<1.24\n" );
				writer.write( "    - tensorflow==2.10\n" );
				writer.write( "    - stardist==0.8.5\n" );
				writer.write( "    - appose\n" );
			}
			envFile.deleteOnExit();
			env = Appose.file( envFile, "environment.yml" ).logDebug().build();
		}
		catch ( IOException e )
		{
			logger.error( "Could not create temporary yml file: {}", e.getMessage(), e );
			return null;
		}
		logger.info( "Python environment created" );
		return env;
	}

	static String generateScript( final String starDistModelPath )
	{
		String script = "import numpy as np" + "\n"
				+ "import appose" + "\n"
				+ "from csbdeep.utils import normalize" + "\n"
				+ "from stardist.models import StarDist3D" + "\n"
				+ "np.random.seed(6)" + "\n"
				+ "axes_normalize = (0, 1, 2)" + "\n"
				+ "print(\"Loading StarDist pretrained 3D model\")" + "\n"
				// + "model = StarDist3D.from_pretrained('3D_demo')" + "\n"
				+ "model = StarDist3D(None, name='stardist-plant-nuclei-3d', basedir=r\"" + starDistModelPath + "\")"
				+ "\n"
				+ "narr = image.ndarray()" + "\n"
				+ "image_normalized = normalize(narr, 1, 99.8, axis=axes_normalize)" + "\n"
				+ "print(\"Image shape:\", image_normalized.shape)" + "\n"
				+ "guessed_tiles = model._guess_n_tiles(image_normalized)" + "\n"
				+ "print(\"Guessed tiling:\", guessed_tiles)" + "\n"
				+ "label_image, details = model.predict_instances(image_normalized, axes='ZYX', n_tiles=guessed_tiles)" + "\n"
				+ "shared = appose.NDArray(image.dtype, image.shape)" + "\n"
				+ "shared.ndarray()[:] = label_image" + "\n"
				+ "task.outputs['label_image'] = shared" + "\n";
		logger.info( "Script: {}", script );
		return script;
	}
}
