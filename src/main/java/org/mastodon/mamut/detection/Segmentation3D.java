package org.mastodon.mamut.detection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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

public abstract class Segmentation3D implements AutoCloseable
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	abstract String generateEnvFileContent();

	abstract String generateScript();

	private final Service pythonWorker;

	private final Map< String, Object > inputs;

	private final StopWatch stopWatch;

	public Segmentation3D() throws IOException
	{
		this.stopWatch = StopWatch.createStarted();
		Environment environment = setUpEnv();
		if ( environment == null )
		{
			logger.error( "Could not create python environment" );
			throw new RuntimeException( "Could not create python environment" );
		}
		stopWatch.split();
		logger.info( "Set up environment. Path: {}. Time elapsed: {}", environment.base(), stopWatch.formatSplitTime() );
		this.pythonWorker = environment.python();
		this.pythonWorker.debug( logger::debug );
		this.inputs = new HashMap<>();
	}

	@Override
	public void close() throws Exception
	{
		stopWatch.stop();
		logger.info( "Segmentation finished, stopping python process. Time elapsed: {}", stopWatch.formatTime() );
		if ( pythonWorker != null )
			pythonWorker.close();
	}

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
			environment = Appose.file( envFile, "environment.yml" )
					.subscribeProgress( ( title, cur, max ) -> logger.info( "{}: {}/{}", title, cur, max ) )
					.subscribeOutput( msg -> logger.info( msg.isEmpty() ? "." : msg ) )
					.subscribeError( msg -> logger.error( msg.isEmpty() ? "." : msg ) ).build();
		}
		catch ( IOException e )
		{
			logger.error( "Could not create temporary yml file: {}", e.getMessage(), e );
			return null;
		}
		return environment;
	}

	public < T extends NativeType< T > > Img< T > segmentImage( final RandomAccessibleInterval< T > inputImage ) throws IOException
	{
		String script = generateScript();
		logger.trace( "Running script:\n{}", script );
		long[] dimensions = inputImage.dimensionsAsLongArray();
		String dimensionsAsString = Arrays.stream( dimensions ).mapToObj( String::valueOf ).collect( Collectors.joining( ", " ) );
		logger.info( "Segmenting image with {} dimensions: ({})", inputImage.numDimensions(), dimensionsAsString );
		try (ShmImg< T > sharedMemoryImage = ShmImg.copyOf( inputImage ))
		{
			stopWatch.split();
			logger.info( "Copied image to shared memory. Time elapsed: {}", stopWatch.formatSplitTime() );
			NDArray ndArray = NDArrays.asNDArray( sharedMemoryImage );
			stopWatch.split();
			logger.info( "Converted image to nd array: {}, Time elapsed: {}", ndArray, stopWatch.formatSplitTime() );
			inputs.put( "image", ndArray );

			Service.Task task = pythonWorker.task( script, inputs );
			stopWatch.split();
			logger.info( "Created python task. Time elapsed: {}", stopWatch.formatSplitTime() );
			task.listen( getTaskListener( stopWatch, task ) );
			try
			{
				task.waitFor();
			}
			catch ( InterruptedException e )
			{
				logger.error( "Task interrupted: {}", e.getMessage(), e );
				return null;
			}
			// Verify that it worked.
			if ( task.status != Service.TaskStatus.COMPLETE )
				throw new PythonRuntimeException( "Python task failed with error: " + task.error );
			stopWatch.split();
			logger.info( "Python task completed. Total time {}", stopWatch.formatSplitTime() );
			NDArray segmentedImageArray = ( NDArray ) task.outputs.get( "label_image" );
			ShmImg< T > segmentedImage = new ShmImg<>( segmentedImageArray );
			stopWatch.split();
			logger.info( "Converted output to image. Time elapsed: {}", stopWatch.formatSplitTime() );
			return segmentedImage;
		}
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
