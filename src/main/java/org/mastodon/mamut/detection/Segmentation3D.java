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
import java.util.Objects;
import java.util.function.Consumer;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.appose.NDArrays;
import net.imglib2.appose.ShmImg;
import net.imglib2.img.Img;
import net.imglib2.type.NativeType;

import org.apache.commons.lang3.time.StopWatch;
import org.apposed.appose.Appose;
import org.apposed.appose.Environment;
import org.apposed.appose.NDArray;
import org.apposed.appose.Service;
import org.apposed.appose.TaskEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Segmentation3D
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	abstract String generateEnvFileContent();

	abstract String generateScript();

	private Environment setUpEnv()
	{
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
		return environment;
	}

	public < T extends NativeType< T > > Img< T > segmentImage( final RandomAccessibleInterval< T > inputImage )
	{
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		Img< T > sharedMemoryImage = ShmImg.copyOf( inputImage );
		stopWatch.split();
		logger.info( "Copied image to shared memory. Time elapsed: {} ms", stopWatch.formatSplitTime() );
		Environment environment = setUpEnv();
		stopWatch.split();
		logger.info( "Set up environment. Time elapsed: {} ms", stopWatch.formatSplitTime() );
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
			stopWatch.split();
			logger.info( "Created inputs. Time elapsed: {} ms", stopWatch.formatSplitTime() );
			String script = generateScript();
			logger.trace( "Script: \n{}", script );
			// Run the script!
			Service.Task task = python.task( script, inputs );
			stopWatch.split();
			logger.info( "Created python task. Time elapsed: {} ms", stopWatch.formatSplitTime() );
			task.listen( getTaskListener( stopWatch, task ) );
			task.waitFor();
			// Verify that it worked.
			if ( task.status != Service.TaskStatus.COMPLETE )
				throw new PythonRuntimeException( "Python task failed with error: " + task.error );
			stopWatch.stop();
			logger.info( "Python task completed. Total time {} ms", stopWatch.formatTime() );
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

	private Consumer< TaskEvent > getTaskListener( final StopWatch stopWatch, final Service.Task task )
	{
		return taskEvent -> {
			switch ( taskEvent.responseType )
			{
			case UPDATE:
				stopWatch.split();
				logger.info( "Task update: {}. Time elapsed: {}", task.message, stopWatch.formatSplitTime() );
				break;
			case LAUNCH:
				stopWatch.split();
				logger.info( "Task launched. Time elapsed: {}", stopWatch.formatSplitTime() );
				break;
			case COMPLETION:
				stopWatch.split();
				logger.info( "Task completed. Time elapsed: {}", stopWatch.formatSplitTime() );
				break;
			}
		};
	}

	private static void logEnvFile( final File envFile )
	{
		// Read the file to check its contents
		try (BufferedReader reader = new BufferedReader( new FileReader( envFile ) ))
		{
			String line;
			StringBuilder content = new StringBuilder();
			while ( ( line = reader.readLine() ) != null )
			{
				content.append( line ).append( "\n" );
			}
			logger.trace( "Environment file content:\n{}", content );
		}
		catch ( IOException e )
		{
			logger.debug( "Error reading env file: {}", e.getMessage() );
		}
	}
}
