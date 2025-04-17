package org.mastodon.mamut.detection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.appose.NDArrays;
import net.imglib2.appose.ShmImg;
import net.imglib2.img.Img;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import org.apposed.appose.Appose;
import org.apposed.appose.Environment;
import org.apposed.appose.NDArray;
import org.apposed.appose.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Segmentation3D
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	abstract String generateEnvFileContent();

	abstract String generateScript();

	private Environment setUpEnv()
	{
		logger.info( "Setting up environment" );
		Environment environment;
		try
		{
			File envFile = Files.createTempFile( "env", "yml" ).toFile();
			String content = generateEnvFileContent();
			try (BufferedWriter writer = new BufferedWriter( new FileWriter( envFile ) ))
			{
				writer.write( content );
			}
			envFile.deleteOnExit();
			logEnvFile( envFile );
			environment = Appose.file( envFile, "environment.yml" ).logDebug().build();
		}
		catch ( IOException e )
		{
			logger.error( "Could not create temporary yml file: {}", e.getMessage(), e );
			return null;
		}
		logger.info( "Python environment created" );
		return environment;
	}

	public < T extends NativeType< T > & RealType< T > > Img< T > segmentImage( final RandomAccessibleInterval< T > inputImage )
	{
		Img< T > sharedMemoryImage = ShmImg.copyOf( inputImage );
		Environment environment = setUpEnv();
		if ( environment == null )
		{
			logger.error( "Could not create python environment" );
			return null;
		}
		try (Service python = environment.python())
		{
			// Store the image into a map of inputs to the Python script.
			final Map< String, Object > inputs = new HashMap<>();
			inputs.put( "image", NDArrays.asNDArray( sharedMemoryImage ) );

			String script = generateScript();
			logger.info( "Script: \n{}", script );
			// Run the script!
			Service.Task task = python.task( script, inputs );
			python.debug( logger::info );
			task.waitFor();
			// Verify that it worked.
			if ( task.status != Service.TaskStatus.COMPLETE )
				throw new PythonRuntimeException( "Python task failed with error: " + task.error );

			logger.info( "Python task completed" );
			NDArray labelImageArray = ( NDArray ) task.outputs.get( "label_image" );
			return new ShmImg<>( labelImageArray );
		}
		catch ( IOException e )
		{
			logger.error( "Could not create python service: {}", e.getMessage(), e );
		}
		catch ( InterruptedException e )
		{
			logger.error( "Python interrupted: {}", e.getMessage(), e );
		}
		return null;
	}

	private static void logEnvFile( final File envFile )
	{
		// Read the file to check its contents
		try (BufferedReader reader = new BufferedReader( new FileReader( envFile ) ))
		{
			String line;
			String content = "";
			while ( ( line = reader.readLine() ) != null )
			{
				content += line + "\n";
			}
			logger.info( "Environment file content:\n{}", content );
		}
		catch ( IOException e )
		{
			logger.debug( "Error reading env file: {}", e.getMessage() );
		}
	}
}
