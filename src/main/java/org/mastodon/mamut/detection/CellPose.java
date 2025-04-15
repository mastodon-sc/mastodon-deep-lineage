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

public class CellPose
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	// private constructor to prevent instantiation
	private CellPose()
	{

	}

	static Environment setUpEnv() throws IOException
	{
		Environment env;
		try
		{
			File envFile = Files.createTempFile( "env", "yml" ).toFile();
			try (BufferedWriter writer = new BufferedWriter( new FileWriter( envFile ) ))
			{
				writer.write( "name: cellpose\n" );
				writer.write( "channels:\n" );
				writer.write( "  - nvidia\n" );
				writer.write( "  - pytorch\n" );
				writer.write( "  - conda-forge\n" );
				writer.write( "dependencies:\n" );
				writer.write( "  - python=3.10\n" );
				writer.write( "  - pip\n" );
				writer.write( "  - pip:\n" );
				writer.write( "    - cellpose[gui]\n" );
				writer.write( "    - appose\n" );
				writer.write( "  - pytorch\n" );
				writer.write( "  - pytorch-cuda=11.8\n" );
				writer.write( "  - numpy=2.0.2\n" );
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

	static String generateScript()
	{

		String script = "import numpy as np" + "\n"
				+ "from cellpose import models" + "\n"
				+ "import logging" + "\n"
				+ "narr = image.ndarray()" + "\n"
				+ "print(\"Image shape: \", image.shape)" + "\n"
				+ "model = models.Cellpose(model_type=model_type, gpu=True)" + "\n\n"
				+ "logging.info(\"Starting Cellpose segmentation...\")" + "\n"
				+ "logging.info(\"Starting Cellpose segmentation...\")" + "\n\n"
				+ "segmentation, flows, styles, diams = model.eval(image, diameter=None, channels=[0, 0], do_3D=True, anisotropy=1.0, z_axis=0, normalize=True)"
				+ "\n"
				+ "logging.info(\"Segmentation complete.\")" + "\n"
				+ "shared = appose.NDArray(image.dtype, image.shape)" + "\n"
				+ "shared.ndarray()[:] = segmentation" + "\n"
				+ "task.outputs['label_image'] = shared" + "\n";
		logger.info( "Script: {}", script );
		return script;
	}
}
