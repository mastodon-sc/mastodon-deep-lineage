package org.mastodon.mamut.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.lang3.time.StopWatch;
import org.apposed.appose.Environment;
import org.apposed.appose.Service;
import org.apposed.appose.TaskEvent;
import org.mastodon.mamut.detection.PythonRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class providing a framework for Python-based processing.
 * It manages the Python environment setup.<br>
 * Subclasses are required to provide details, such as generating
 * the environment file content and the script for the python process segmentation.<br>
 * This class facilitates the transfer of data between the Java and Python environments, tracks task progress and execution time.
 */
public abstract class ApposeProcess implements AutoCloseable
{

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	protected abstract String generateEnvFileContent();

	protected abstract String generateScript();

	protected final Service pythonWorker;

	protected final StopWatch stopWatch;

	protected final Map< String, Object > inputs;

	protected ApposeProcess() throws IOException
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
			logger.info( "Finished python process. Time elapsed: {}", stopWatch.formatTime() );
		if ( pythonWorker != null )
		{
			try
			{
				pythonWorker.close();
			}
			catch ( Exception e )
			{
				logger.warn( "Could not properly close python worker: {}", e.getMessage() );
			}
		}
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
			logEnvFile( envFile );
			environment = org.apposed.appose.Appose.file( envFile, "environment.yml" )
					.subscribeProgress( ( title, cur, max ) -> logger.info( "{}: {}/{}", title, cur, max ) )
					.subscribeOutput( msg -> {
						if ( !msg.isEmpty() )
							logger.info( msg );
					} )
					.subscribeError( msg -> {
						if ( !msg.isEmpty() )
							logger.error( msg );
					} ).build();
			Files.deleteIfExists( envFile.toPath() );
		}
		catch ( IOException e )
		{
			logger.error( "Could not create temporary yml file: {}", e.getMessage(), e );
			return null;
		}
		return environment;
	}

	protected Consumer< TaskEvent > getTaskListener( final StopWatch stopWatch, final Service.Task task )
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
		return "    - appose==0.4.0\n";
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
