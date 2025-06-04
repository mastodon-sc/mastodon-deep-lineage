package org.mastodon.mamut.appose;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.lang3.time.StopWatch;
import org.apposed.appose.Appose;
import org.apposed.appose.Environment;
import org.apposed.appose.Service;

public class ApposeReuseEnvironmentExample
{
	public static void main( String[] args ) throws IOException, InterruptedException
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
		Environment env = Appose.file( envFile, "environment.yml" ).logDebug().build();
		System.out.println( "Created environment" );
		StopWatch stopWatch = StopWatch.createStarted();

		String script = "import numpy as np" + "\n"
				+ "from cellpose import models" + "\n"
				+ "import appose" + "\n\n"
				+ "5 + 6" + "\n";

		try (Service python = env.python())
		{
			stopWatch.split();
			System.out.println( "Python service started: " + stopWatch.formatSplitTime() );
			Service.Task task1 = python.task( script );
			task1.waitFor();
			Object result1 = task1.outputs.get( "result" );
			System.out.println( "result1: " + result1 );
			stopWatch.split();
			System.out.println( "Python task1 finished: " + stopWatch.formatSplitTime() );

			Service.Task task2 = python.task( script );
			task2.waitFor();
			Object result2 = task2.outputs.get( "result" );
			System.out.println( "result2: " + result2 );

			stopWatch.split();
			System.out.println( "Python task2 finished: " + stopWatch.formatSplitTime() );
		}
	}
}
