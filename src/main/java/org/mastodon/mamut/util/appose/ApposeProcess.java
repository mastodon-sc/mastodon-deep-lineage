package org.mastodon.mamut.util.appose;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.apache.commons.lang3.time.StopWatch;
import org.apposed.appose.Service;
import org.apposed.appose.TaskEvent;
import org.mastodon.mamut.detection.PythonRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An abstract class that provides a base for executing Python-based tasks within a Java application.
 * This class uses a Python service to run tasks, handles task status, and logs execution progress and errors.
 * Subclasses must implement the {@link #generateScript()} method to provide the specific Python script to be executed.
 */
public abstract class ApposeProcess
{

	private static final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private final org.scijava.log.Logger scijavaLogger;

	public static final String APPOSE_PYTHON_VERSION = "0.7.2";

	protected final Service pythonWorker;

	protected final StopWatch stopWatch;

	protected final Map< String, Object > inputs;

	protected ApposeProcess( final Service pythonService, final @Nullable org.scijava.log.Logger scijavaLogger )
	{
		this.scijavaLogger = scijavaLogger;
		this.stopWatch = StopWatch.createStarted();
		stopWatch.split();
		this.pythonWorker = pythonService;
		// this.pythonWorker.debug( logger::info );
		this.inputs = new HashMap<>();
	}

	protected abstract String generateScript();

	protected Consumer< TaskEvent > getTaskListener( final StopWatch stopWatch, final Service.Task task )
	{
		return taskEvent -> {
			stopWatch.split();
			switch ( taskEvent.responseType )
			{
			case UPDATE:
				logger.info( "Task update: {}. Time elapsed: {}", taskEvent.message, stopWatch.formatSplitTime() );
				if ( scijavaLogger != null )
					scijavaLogger.info( "Task update: " + taskEvent.message + ". Time elapsed: " + stopWatch.formatSplitTime() );
				break;
			case LAUNCH:
				logger.info( "Task launched. Time elapsed: {}", stopWatch.formatSplitTime() );
				if ( scijavaLogger != null )
					scijavaLogger.info( "Task launched: Time elapsed: " + stopWatch.formatSplitTime() );
				break;
			case COMPLETION:
				logger.info( "Task completed. Time elapsed: {}", stopWatch.formatSplitTime() );
				if ( scijavaLogger != null )
					scijavaLogger.info( "Task completed: Time elapsed: " + stopWatch.formatSplitTime() );
				break;
			case FAILURE:
				logger.error( "Task failed with error: {}. Time elapsed: {}", task.error, stopWatch.formatSplitTime() );
				if ( scijavaLogger != null )
					scijavaLogger.error( "Task failed with error: " + task.error + ". Time elapsed: " + stopWatch.formatSplitTime() );
				break;
			case CRASH:
				logger.error( "Task crashed with error. Error: {}. Time elapsed: {}", task.error, stopWatch.formatSplitTime() );
				if ( scijavaLogger != null )
					scijavaLogger.error( "Task crashed with error: " + task.error + ". Time elapsed: " + stopWatch.formatSplitTime() );
				break;
			default:
				if ( scijavaLogger != null )
					scijavaLogger.warn( "Unhandled task event: " + taskEvent.responseType );
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

	protected Service.Task runScript()
	{
		String script = generateScript();
		logger.info( "Run script:\n{}", script );
		Service.Task task = pythonWorker.task( script, inputs, null );
		stopWatch.split();
		if ( logger.isInfoEnabled() )
			logger.info( "Created python task. Time elapsed: {}", stopWatch.formatSplitTime() );
		task.listen( getTaskListener( stopWatch, task ) );
		if ( isPythonTaskInterrupted( task ) )
			return null;

		// Verify that it worked.
		if ( task.status != Service.TaskStatus.COMPLETE )
			throw new PythonRuntimeException( "Python task failed with error: " + task.error );

		stopWatch.split();
		if ( logger.isInfoEnabled() )
			logger.info( "Python task completed. Total time {}", stopWatch.formatSplitTime() );
		return task;
	}

	protected Service.Task runScriptWithRetries( final int attempt, final int maxRetries ) throws IOException
	{
		try
		{
			return runScript();
		}
		catch ( PythonRuntimeException e )
		{
			if ( attempt <= maxRetries )
			{
				logger.warn( "Python runtime exception on attempt {}/{}. Retrying...", attempt, maxRetries );
				return runScriptWithRetries( attempt + 1, maxRetries );
			}
			else
			{
				logger.error( "Python runtime exception on final attempt {}/{}. No more retries.", attempt, maxRetries );
				throw e;
			}
		}
	}
}
