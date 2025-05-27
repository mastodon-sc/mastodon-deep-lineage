package org.mastodon.mamut.appose;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import net.imglib2.appose.NDArrays;
import net.imglib2.appose.ShmImg;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;

import org.apposed.appose.Appose;
import org.apposed.appose.Environment;
import org.apposed.appose.Service;

public class ApposeNumpyCellposeEnvExample
{
	public static void main( String[] args ) throws IOException, InterruptedException
	{
		Img< FloatType > img = ArrayImgs.floats( 10, 10, 10 );
		Img< FloatType > shmImg = ShmImg.copyOf( img );

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
			writer.write( "    - cellpose\n" );
			writer.write( "    - appose\n" );
			writer.write( "  - pytorch\n" );
			writer.write( "  - pytorch-cuda=11.8\n" );
		}
		envFile.deleteOnExit();
		Environment env = Appose.file( envFile, "environment.yml" ).logDebug().build();
		System.out.println( "Created environment" );

		String script = "import numpy" + "\n"
				+ "from cellpose import models" + "\n"
				+ "import appose" + "\n"
				+ "task.update(message=\"Imports completed\")" + "\n"
				+ "\n";

		try (Service python = env.python())
		{
			python.debug( System.out::println );

			// Store our Img into a map of inputs to the Python script.
			final Map< String, Object > inputs = new HashMap<>();
			inputs.put( "image", NDArrays.asNDArray( shmImg ) );

			// Run the script using provided inputs.
			// Service.Task task = python.task( script ); // This line works
			Service.Task task = python.task( script, inputs ); // This line throws an exception
			task.waitFor();

			// Verify that it worked.
			if ( task.status != Service.TaskStatus.COMPLETE )
			{
				throw new RuntimeException( "Python script failed with error: " + task.error );
			}
			System.out.println( "Python script completed" );
		}
	}
}
