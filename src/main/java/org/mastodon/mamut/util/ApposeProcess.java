package org.mastodon.mamut.util;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandles;
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

	public static final String APPOSE_PYTHON_VERSION = "0.7.1";

	protected abstract String generateEnvFileContent();

	protected abstract String generateScript();

	protected abstract String generateImportStatements();

	protected final Service pythonWorker;

	protected final StopWatch stopWatch;

	protected final Map< String, Object > inputs;

	protected Service.Task currentTask;

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
		runInitScript();
		// this.pythonWorker.debug( logger::info );
		this.inputs = new HashMap<>();
	}

	@Override
	public void close() throws Exception
	{
		stopWatch.stop();
		if ( logger.isInfoEnabled() )
			logger.info( "Finished python process. Time elapsed: {}", stopWatch.formatTime() );
		if ( pythonWorker != null && pythonWorker.isAlive() )
		{
			try
			{
				pythonWorker.close();
			}
			catch ( Exception e )
			{
				logger.warn( "Could not properly close python worker: {}", e.getMessage(), e );
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
			String content = generateEnvFileContent();
			environment = org.apposed.appose.Appose.mamba().scheme( "environment.yml" ).content( content ).logDebug()
					.subscribeProgress( ( title, cur, max ) -> logger.info( "{}: {}/{}", title, cur, max ) )
					.subscribeOutput( logger::info )
					.subscribeError( logger::error ).build();
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
			case CRASH:
				logger.error( "Task crashed with error. Error: {}. Time elapsed: {}", task.error, stopWatch.formatSplitTime() );
				break;
			default:
				logger.warn( "Unhandled task event: {}.", taskEvent.responseType );
				break;
			}
		};
	}

	protected static boolean isPythonTaskInterrupted( final Service.Task task )
	{
		try
		{
			task.waitFor();
		}
		catch ( InterruptedException e )
		{
			logger.error( "Task interrupted: {}", e.getMessage(), e );
			Thread.currentThread().interrupt();
			return true;
		}
		return false;
	}

	protected Service.Task runScript() throws IOException
	{
		runInitScript();
		Service.Task doImports = pythonWorker.task( generateImportStatements(), "main" );
		doImports.listen( getTaskListener( stopWatch, doImports ) );
		try
		{
			doImports.waitFor();
		}
		catch ( InterruptedException e )
		{
			logger.error( "Import task interrupted: {}", e.getMessage(), e );
			Thread.currentThread().interrupt();
		}

		String script = generateScript();
		currentTask = pythonWorker.task( script, inputs, null );
		stopWatch.split();
		if ( logger.isInfoEnabled() )
			logger.info( "Created python task. Time elapsed: {}", stopWatch.formatSplitTime() );
		currentTask.listen( getTaskListener( stopWatch, currentTask ) );
		if ( isPythonTaskInterrupted( currentTask ) )
			return null;

		// Verify that it worked.
		if ( currentTask.status != Service.TaskStatus.COMPLETE )
			throw new PythonRuntimeException( "Python task failed with error: " + currentTask.error );

		stopWatch.split();
		if ( logger.isInfoEnabled() )
			logger.info( "Python task completed. Total time {}", stopWatch.formatSplitTime() );
		return currentTask;
	}

	public void cancel()
	{
		pythonWorker.kill();
	}

	protected void runInitScript()
	{
		if ( isWindows() )
			pythonWorker.init( "import numpy" );
	}

	protected static boolean isWindows()
	{
		String os = System.getProperty( "os.name" ).toLowerCase();
		return os.contains( "win" );
	}
}
