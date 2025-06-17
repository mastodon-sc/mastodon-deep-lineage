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
package org.mastodon.mamut.detection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Paths;
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

/**
 * Abstract class providing a framework for image segmentation using Python-based processing.
 * It manages the Python environment setup, execution of the segmentation in the Python environment, and results retrieval.<br>
 * Subclasses are required to provide details, such as generating
 * the environment file content and the script for the actual image segmentation.<br>
 * This class facilitates the transfer of images between the Java and Python environments
 * using shared memory and tracks task progress and execution time.
 */
public abstract class Segmentation implements AutoCloseable
{
	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	protected abstract String generateEnvFileContent();

	protected abstract String generateScript();

	private final Service pythonWorker;

	private final Map< String, Object > inputs;

	private final StopWatch stopWatch;

	protected Segmentation() throws IOException
	{
		this.stopWatch = StopWatch.createStarted();
		Environment environment = setUpEnv();
		if ( environment == null )
		{
			logger.error( "Could not create python environment" );
			throw new PythonRuntimeException( "Could not create python environment" );
		}
		stopWatch.split();
		if ( logger.isInfoEnabled() )
			logger.info( "Set up environment. Path: {}. Time elapsed: {}", environment.base(), stopWatch.formatSplitTime() );
		this.pythonWorker = environment.python();
		// this.pythonWorker.debug( logger::info );
		this.inputs = new HashMap<>();
	}

	@Override
	public void close() throws Exception
	{
		stopWatch.stop();
		if ( logger.isInfoEnabled() )
			logger.info( "Segmentation finished, stopping python process. Time elapsed: {}", stopWatch.formatTime() );
		if ( pythonWorker != null )
			pythonWorker.close();
	}

	private Environment setUpEnv()
	{
		Environment environment;
		try
		{
			File envFileDirectory = Paths.get( System.getProperty( "user.home" ), ".local", "share", "appose" ).toFile();
			if ( !envFileDirectory.exists() && !envFileDirectory.mkdirs() )
			{
				logger.error( "Failed to create environment directory: {}", envFileDirectory.getAbsolutePath() );
				throw new UncheckedIOException( "Failed to create environment directory: " + envFileDirectory.getAbsolutePath(),
						new IOException() );
			}
			File envFile = File.createTempFile( "env", "yml", envFileDirectory );
			String content = generateEnvFileContent();
			try (BufferedWriter writer = new BufferedWriter( new FileWriter( envFile ) ))
			{
				writer.write( content );
			}
			envFile.deleteOnExit();
			logEnvFile( envFile );
			environment = Appose.file( envFile, "environment.yml" )
					.subscribeProgress( ( title, cur, max ) -> logger.info( "{}: {}/{}", title, cur, max ) )
					.subscribeOutput( msg -> {
						if ( !msg.isEmpty() )
							logger.info( msg );
					} )
					.subscribeError( msg -> {
						if ( !msg.isEmpty() )
							logger.error( msg );
					} ).build();
		}
		catch ( IOException e )
		{
			logger.error( "Could not create temporary yml file: {}", e.getMessage(), e );
			return null;
		}
		return environment;
	}

	/**
	 * Segments the input image using the configured Python environment and
	 * returns the segmented image as an {@link Img}.
	 *
	 * @param inputImage the input image to be segmented.
	 * @param <T>        the type of the image.
	 * @return the segmented image.
	 * @throws IOException if there is an error during segmentation.
	 */
	public < T extends NativeType< T > > Img< T > segmentImage( final RandomAccessibleInterval< T > inputImage ) throws IOException
	{
		String script = generateScript();
		logger.debug( "Running script:\n{}", script );
		long[] dimensions = inputImage.dimensionsAsLongArray();
		String dimensionsAsString = Arrays.stream( dimensions ).mapToObj( String::valueOf ).collect( Collectors.joining( ", " ) );
		logger.info( "Segmenting image with {} dimensions: ({})", inputImage.numDimensions(), dimensionsAsString );
		try (ShmImg< T > sharedMemoryImage = ShmImg.copyOf( inputImage ))
		{
			stopWatch.split();
			if ( logger.isInfoEnabled() )
				logger.info( "Copied image to shared memory. Time elapsed: {}", stopWatch.formatSplitTime() );
			NDArray ndArray = NDArrays.asNDArray( sharedMemoryImage );
			stopWatch.split();
			if ( logger.isInfoEnabled() )
				logger.info( "Converted image to nd array: {}, Time elapsed: {}", ndArray, stopWatch.formatSplitTime() );
			inputs.put( "image", ndArray );

			Service.Task task = pythonWorker.task( script, inputs );
			stopWatch.split();
			if ( logger.isInfoEnabled() )
				logger.info( "Created python task. Time elapsed: {}", stopWatch.formatSplitTime() );
			task.listen( getTaskListener( stopWatch, task ) );
			try
			{
				task.waitFor();
			}
			catch ( InterruptedException e )
			{
				logger.error( "Task interrupted: {}", e.getMessage(), e );
				Thread.currentThread().interrupt();
				return null;
			}
			// Verify that it worked.
			if ( task.status != Service.TaskStatus.COMPLETE )
				throw new PythonRuntimeException( "Python task failed with error: " + task.error );
			stopWatch.split();
			if ( logger.isInfoEnabled() )
				logger.info( "Python task completed. Total time {}", stopWatch.formatSplitTime() );
			NDArray segmentedImageArray = ( NDArray ) task.outputs.get( "label_image" );
			ShmImg< T > segmentedImage = new ShmImg<>( segmentedImageArray );
			stopWatch.split();
			if ( logger.isInfoEnabled() )
				logger.info( "Converted output to image. Time elapsed: {}", stopWatch.formatSplitTime() );
			return segmentedImage;
		}
	}

	private Consumer< TaskEvent > getTaskListener( final StopWatch stopWatch, final Service.Task task )
	{
		return taskEvent -> {
			stopWatch.split();
			switch ( taskEvent.responseType )
			{
			case UPDATE:
				logger.info( "Task update: {}. Time elapsed: {}", taskEvent.message, stopWatch.formatSplitTime() );
				break;
			case LAUNCH:
				logger.info( "Task launched. Time elapsed: {}", stopWatch.formatSplitTime() );
				break;
			case COMPLETION:
				logger.info( "Task completed. Time elapsed: {}", stopWatch.formatSplitTime() );
				break;
			case FAILURE:
				logger.error( "Task failed with error: {}. Time elapsed: {}", task.error, stopWatch.formatSplitTime() );
				break;
			default:
				logger.warn( "Unhandled task event: {}.", taskEvent.responseType );
				break;
			}
		};
	}

	protected String getApposeVersion()
	{
		String os = System.getProperty( "os.name" ).toLowerCase();
		if ( os.contains( "win" ) )
			return "    - git+https://github.com/apposed/appose-python.git@dd3f49a35542a8ec1181f176d04890c317ec8182\n"; // latest commit on main branch
		else
			return "    - git+https://github.com/apposed/appose-python.git@fc61f5a9367248d6f1a1cc1e322c82bcf50f1a9d\n"; // latest commit on main-thread-queue branch
		// return "    - appose==0.4.0\n";
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
